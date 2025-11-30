    package com.example.velox.network

    import android.content.Context
    import android.os.Build
    import android.os.Handler
    import android.os.Looper
    import android.util.Log
    import android.widget.Toast
    import androidx.annotation.RequiresApi
    import com.example.velox.login.compose.ReminderFrequency
    import com.example.velox.login.viewModel.Category
    import com.example.velox.login.viewModel.Task
    import com.example.velox.utils.ApiConfig
    import okhttp3.*
    import okhttp3.MediaType.Companion.toMediaTypeOrNull
    import org.json.JSONArray
    import org.json.JSONObject
    import java.io.IOException
    import java.time.LocalDateTime


    object ApiClient {
        private val client = OkHttpClient()

        fun signUp(context: Context, email: String, password: String, repeatPassword: String, onResult: (Boolean) -> Unit) {
            val json = JSONObject().apply {
                put("email", email)
                put("password", password)
                put("re_password", repeatPassword)
                put("name", "Your Name")
            }

            val body = RequestBody.create(
                "application/json; charset=utf-8".toMediaTypeOrNull(),
                json.toString()
            )

            val request = Request.Builder()
                .url("${ApiConfig.BASE_URL}/auth/users/")
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Connection error during registration", Toast.LENGTH_SHORT).show()
                        onResult(false)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    Handler(Looper.getMainLooper()).post {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Activation link sent to email.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                        onResult(response.isSuccessful)
                    }
                }
            })
        }

        fun signIn(context: Context, email: String, password: String, onResult: (Boolean) -> Unit) {
            val json = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            val body = RequestBody.create(
                "application/json; charset=utf-8".toMediaTypeOrNull(),
                json.toString()
            )

            val request = Request.Builder()
                .url("${ApiConfig.BASE_URL}/auth/jwt/create/")
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Handler(context.mainLooper).post {
                        Toast.makeText(context, "Connection error during sign-in", Toast.LENGTH_SHORT).show()
                        onResult(false)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string() ?: ""
                    Handler(context.mainLooper).post {
                        if (response.isSuccessful) {
                            val jsonResponse = JSONObject(responseBody)
                            val accessToken = jsonResponse.getString("access")
                            val refreshToken = jsonResponse.getString("refresh")
                            Log.d("ApiClient", "Access Token: $accessToken")
                            Log.d("ApiClient", "Refresh Token: $refreshToken")

                            val prefs = context.getSharedPreferences("velox_prefs", Context.MODE_PRIVATE)
                            prefs.edit()
                                .putString("access_token", accessToken)
                                .putString("refresh_token", refreshToken)
                                .apply()

                            Toast.makeText(context, "Sign-in successful", Toast.LENGTH_SHORT).show()
                            onResult(true)
                        } else {
                            Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                            onResult(false)
                        }
                    }
                }
            })
        }




        fun createTask(context: Context, task: Task, onResult: (Boolean) -> Unit) {
            val accessToken = getAccessToken(context)

            val json = JSONObject().apply {
                put("title", task.title)
                put("category", task.categoryId)
                put("date", task.date)
                put("time_start", task.timeStart)
                put("time_end", task.timeEnd)
                put("reminder", task.hasReminder)
                put("location", task.location)
                put("notes", task.notes)
                put("frequency", task.frequency.name.lowercase())
            }

            val body = RequestBody.create(
                "application/json; charset=utf-8".toMediaTypeOrNull(),
                json.toString()
            )

            val request = Request.Builder()
                .url("${ApiConfig.BASE_URL}/api/tasks/")
                .post(body)
                .addHeader("Authorization", "JWT $accessToken")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Task creation failed", Toast.LENGTH_SHORT).show()
                        onResult(false)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code == 401) {
                        // Попробовать обновить токен и повторить запрос
                        refreshToken(context) { success ->
                            if (success) {
                                createTask(context, task, onResult) // Повторить после обновления токена
                            } else {
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(context, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
                                    onResult(false)
                                }
                            }
                        }
                    } else {
                        Handler(Looper.getMainLooper()).post {
                            val responseBody = response.body?.string() ?: ""
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Task created successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.e("ApiClient", "Task creation failed with status ${response.code}: $responseBody")
                                Toast.makeText(context, "Task creation error: $responseBody", Toast.LENGTH_LONG).show()
                                onResult(false)
                            }
                            onResult(response.isSuccessful)
                        }
                    }
                }
            })
        }

        internal fun refreshToken(context: Context, onResult: (Boolean) -> Unit) {
            val prefs = context.getSharedPreferences("velox_prefs", Context.MODE_PRIVATE)
            val refreshToken = getRefreshToken(context)

            if (refreshToken == null) {
                onResult(false)
                return
            }

            val json = JSONObject().apply {
                put("refresh", refreshToken)
            }

            val body = RequestBody.create(
                "application/json; charset=utf-8".toMediaTypeOrNull(),
                json.toString()
            )

            val request = Request.Builder()
                .url("${ApiConfig.BASE_URL}/auth/jwt/refresh/")
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post {
                        onResult(false)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        val jsonResponse = JSONObject(responseBody)
                        val newAccessToken = jsonResponse.getString("access")
                        prefs.edit().putString("access_token", newAccessToken).apply()
                        Handler(Looper.getMainLooper()).post {
                            onResult(true)
                        }
                    } else {
                        Handler(Looper.getMainLooper()).post {
                            onResult(false)
                        }
                    }
                }
            })
        }

        fun checkAuthAndProceed(context: Context, onResult: (Boolean) -> Unit) {
            val access = getAccessToken(context)
            val refresh = getRefreshToken(context)

            if (access == null || refresh == null) {
                Handler(Looper.getMainLooper()).post {
                    onResult(false)
                }
                return
            }

            val request = Request.Builder()
                .url("${ApiConfig.BASE_URL}/api/tasks/")
                .get()
                .addHeader("Authorization", "JWT $access")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post {
                        onResult(false)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code == 401) {
                        refreshToken(context) { refreshed ->
                            Handler(Looper.getMainLooper()).post {
                                onResult(refreshed)
                            }
                        }
                    } else {
                        Handler(Looper.getMainLooper()).post {
                            onResult(true)
                        }
                    }
                }
            })
        }
        fun getCategories(context: Context, onResult: (List<Category>?) -> Unit) {
            val accessToken = getAccessToken(context)

            val request = Request.Builder()
                .url("${ApiConfig.BASE_URL}/api/categories/")
                .get()
                .addHeader("Authorization", "JWT $accessToken")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post {
                        onResult(null)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    if (!response.isSuccessful || responseBody == null) {
                        Handler(Looper.getMainLooper()).post {
                            onResult(null)
                        }
                        return
                    }

                    try {
                        val jsonArray = JSONArray(responseBody)
                        val categories = mutableListOf<Category>()
                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            categories.add(
                                Category(
                                    id = item.getInt("id"),
                                    name = item.getString("name")
                                )
                            )
                        }

                        Handler(Looper.getMainLooper()).post {
                            onResult(categories)
                        }
                    } catch (e: Exception) {
                        Handler(Looper.getMainLooper()).post {
                            onResult(null)
                        }
                    }
                }
            })
        }

        fun getTaskById(context: Context, taskId: String, onResult: (Task?) -> Unit) {
            val accessToken = getAccessToken(context)

            val request = Request.Builder()
                .url("${ApiConfig.BASE_URL}/api/tasks/$taskId/")  // предположим, что API поддерживает получение одной задачи по id
                .get()
                .addHeader("Authorization", "JWT $accessToken")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post {
                        onResult(null)
                    }
                }

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    if (!response.isSuccessful || responseBody == null) {
                        Handler(Looper.getMainLooper()).post {
                            onResult(null)
                        }
                        return
                    }

                    try {
                        val item = JSONObject(responseBody)

                        val date = item.getString("date")
                        val timeStart = item.getString("time_start")
                        val localDateTime = LocalDateTime.parse("${date}T${timeStart}")

                        val categoryString = item.getString("category") // "Category object (14)"
                        val categoryId = Regex("""\((\d+)\)""").find(categoryString)?.groupValues?.get(1)?.toIntOrNull() ?: -1

                        val task = Task(
                            id = item.getInt("id").toString(),
                            title = item.getString("title"),
                            categoryId = categoryId,
                            date = item.getString("date"),
                            timeStart = item.getString("time_start"),
                            timeEnd = item.getString("time_end"),
                            hasReminder = item.getBoolean("reminder"),
                            location = item.getString("location"),
                            notes = item.getString("notes"),
                            frequency = ReminderFrequency.valueOf(
                                item.getString("frequency").uppercase()
                            ),
                            localDateTime = localDateTime
                        )

                        Handler(Looper.getMainLooper()).post {
                            onResult(task)
                        }
                    } catch (e: Exception) {
                        Handler(Looper.getMainLooper()).post {
                            onResult(null)
                        }
                    }
                }
            })
        }


        fun deleteTaskById(context: Context, taskId: String, onResult: (Boolean) -> Unit) {
            val accessToken = getAccessToken(context)

            val request = Request.Builder()
                .url("${ApiConfig.BASE_URL}/api/tasks/$taskId/")  // URL для удаления задачи по id
                .delete()
                .addHeader("Authorization", "JWT $accessToken")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Ошибка запроса — сообщаем false
                    Handler(Looper.getMainLooper()).post {
                        onResult(false)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    // Успех, если код ответа 204 No Content или 200 OK — считаем удаление успешным
                    val success = response.isSuccessful && (response.code == 204 || response.code == 200)
                    Handler(Looper.getMainLooper()).post {
                        onResult(success)
                    }
                }
            })
        }





        fun getTasks(context: Context, onResult: (List<Task>?) -> Unit) {
            val accessToken = getAccessToken(context)

            val request = Request.Builder()
                .url("${ApiConfig.BASE_URL}/api/tasks/")
                .get()
                .addHeader("Authorization", "JWT $accessToken")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post {
                        onResult(null)
                    }
                }

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call, response: Response) {

                    val responseBody = response.body?.string()
                    if (!response.isSuccessful || responseBody == null) {
                        Handler(Looper.getMainLooper()).post {
                            onResult(null)
                        }
                        return
                    }
                    Log.d("getTasks", "Response code: ${response.code}")
                    Log.d("getTasks", "Response body: $responseBody")


                    try {
                        val jsonArray = JSONArray(responseBody)
                        val taskList = mutableListOf<Task>()

                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            val date = item.getString("date")
                            val timeStart = item.getString("time_start")
                            val localDateTime = LocalDateTime.parse("${date}T${timeStart}")

                            val categoryString = item.getString("category") // "Category object (14)"
                            val categoryId = Regex("""\((\d+)\)""").find(categoryString)?.groupValues?.get(1)?.toIntOrNull() ?: -1

                            val task = Task(

                                id = item.getInt("id").toString(),
                                title = item.getString("title"),
                                categoryId = categoryId,
                                date = item.getString("date"),
                                timeStart = item.getString("time_start"),
                                timeEnd = item.getString("time_end"),
                                hasReminder = item.getBoolean("reminder"),
                                location = item.getString("location"),
                                notes = item.getString("notes"),
                                frequency = ReminderFrequency.valueOf(
                                    item.getString("frequency").uppercase()
                                ),
                                localDateTime = localDateTime
                            )

                            taskList.add(task)

                        }

                        Handler(Looper.getMainLooper()).post {
                            onResult(taskList)
                        }
                    } catch (e: Exception) {
                        Handler(Looper.getMainLooper()).post {
                            onResult(null)
                        }
                    }
                }
            })
        }


        fun editTask(context: Context, task: Task, onResult: (Boolean) -> Unit) {
            val accessToken = getAccessToken(context)

            try {
                val json = JSONObject().apply {
                    put("title", task.title)
                    put("category", task.categoryId)  // Просто ID категории (число)
                    put("date", task.date)           // Формат "YYYY-MM-DD"
                    put("time_start", task.timeStart) // Формат "HH:mm"
                    put("time_end", task.timeEnd)     // Формат "HH:mm"
                    put("reminder", task.hasReminder)
                    put("location", task.location)
                    put("notes", task.notes)
                    put("frequency", task.frequency.name.lowercase()) // lowercase
                }

                Log.d("EditTask", "Sending JSON: ${json.toString()}")

                val body = RequestBody.create(
                    "application/json; charset=utf-8".toMediaTypeOrNull(),
                    json.toString()
                )

                val request = Request.Builder()
                    .url("${ApiConfig.BASE_URL}/api/tasks/${task.id}")
                    .patch(body)  // Или .put(body) если PATCH не работает
                    .addHeader("Authorization", "JWT $accessToken")
                    .addHeader("Content-Type", "application/json")
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
                            onResult(false)
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string() ?: ""
                        when {
                            response.code == 401 -> refreshToken(context) { success ->
                                if (success) editTask(context, task, onResult)
                                else {
                                    Handler(Looper.getMainLooper()).post {
                                        Toast.makeText(context, "Session expired", Toast.LENGTH_SHORT).show()
                                        onResult(false)
                                    }
                                }
                            }
                            response.isSuccessful -> {
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(context, "Task updated", Toast.LENGTH_SHORT).show()
                                    onResult(true)
                                }
                            }
                            else -> {
                                Handler(Looper.getMainLooper()).post {
                                    val error = try {
                                        JSONObject(responseBody).getString("detail")
                                            ?: "Error ${response.code}"
                                    } catch (e: Exception) {
                                        "Error ${response.code}"
                                    }
                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                    onResult(false)
                                }
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    onResult(false)
                }
            }
        }




        private fun getAccessToken(context: Context): String? {
            return context.getSharedPreferences("velox_prefs", Context.MODE_PRIVATE)
                .getString("access_token", null)
        }

        private fun getRefreshToken(context: Context): String? {
            return context.getSharedPreferences("velox_prefs", Context.MODE_PRIVATE)
                .getString("refresh_token", null)
        }







    }
