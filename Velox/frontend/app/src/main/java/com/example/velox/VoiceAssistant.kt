package com.example.velox

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.velox.utils.ApiConfig
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import java.time.LocalDate
import java.util.Locale

object VoiceAssistant {

    private var tts: TextToSpeech? = null
    private val client = OkHttpClient()

    // Вспомогательная функция, чтобы безопасно выполнять код на UI-потоке
    private fun runOnUi(context: Context, block: () -> Unit) {
        Handler(Looper.getMainLooper()).post { block() }
    }

    fun init(context: Context) {
        if (tts != null) return  // уже инициализировано

        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {

                val ru = Locale("ru", "RU")
                tts?.language = ru

                val bestVoice = tts?.voices
                    ?.filter { it.locale == ru }
                    ?.sortedByDescending { v ->
                        (if (v.isNetworkConnectionRequired) 1 else 0) +
                                (if (v.quality == Voice.QUALITY_HIGH) 10 else 0)
                    }
                    ?.firstOrNull()

                if (bestVoice != null) {
                    tts?.voice = bestVoice
                }

                tts?.setPitch(1.0f)
                tts?.setSpeechRate(1.0f)

                // 👇 ВАЖНО: прогреть движок, чтобы не было шумов
                tts?.speak("", TextToSpeech.QUEUE_FLUSH, null, "warmup")
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatTimeForSpeech(time: String): String {
        return try {
            val parts = time.split(":")
            val h = parts[0].toInt()
            val m = parts[1].toInt()

            // Преобразование часов в 12-часовой формат
            val hour12 = when {
                h == 0 -> 12
                h > 12 -> h - 12
                else -> h
            }

            val period = when {
                h < 12 -> "утра"
                h == 12 -> "дня"
                h in 13..17 -> "дня"
                else -> "вечера"
            }

            when {
                h == 0 && m == 0 -> "полночь"
                h == 12 && m == 0 -> "полдень"
                m == 0 -> "$hour12 часов $period"
                else -> "$hour12 часов $m минут $period"
            }
        } catch (e: Exception) {
            time
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatDateForSpeech(date: LocalDate): String {
        val day = date.dayOfMonth
        val month = when (date.monthValue) {
            1 -> "января"
            2 -> "февраля"
            3 -> "марта"
            4 -> "апреля"
            5 -> "мая"
            6 -> "июня"
            7 -> "июля"
            8 -> "августа"
            9 -> "сентября"
            10 -> "октября"
            11 -> "ноября"
            12 -> "декабря"
            else -> ""
        }

        return "$day $month"
    }




    @RequiresApi(Build.VERSION_CODES.O)
    fun speakTasksForDate(context: Context, date: LocalDate) {
        val prefs = context.getSharedPreferences("velox_prefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("access_token", null)

        if (accessToken == null) {
            runOnUi(context) {
                Toast.makeText(context, "Токен не найден. Войдите заново.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}/api/tasks/")
            .addHeader("Authorization", "JWT $accessToken")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUi(context) {
                    Toast.makeText(context, "Ошибка сети: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    runOnUi(context) {
                        Toast.makeText(context, "Ошибка сервера: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                try {
                    val jsonArray = JSONArray(body)
                    val dateString = date.toString()

                    val tasksForDay = mutableListOf<String>()

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        if (item.optString("date") == dateString) {
                            val title = item.optString("title", "Без названия")
                            val time = item.optString("time_start", "")
                            val location = item.optString("location", "").trim()

                            val line = buildString {
                                if (time.isNotBlank()) append("${formatTimeForSpeech(time)} — ")
                                append(title)
                                if (location.isNotEmpty()) append(" в $location")
                            }

                            tasksForDay.add(line)


                        }
                    }

                    val textToRead = when {
                        tasksForDay.isEmpty() ->
                            "На ${formatDateForSpeech(date)} задач нет."
                        else ->
                            "Задачи на ${formatDateForSpeech(date)}: " + tasksForDay.joinToString(". ")
                    }


                    runOnUi(context) {
                        speakText(context, textToRead)
                    }

                } catch (e: Exception) {
                    runOnUi(context) {
                        Toast.makeText(context, "Ошибка обработки задач", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun speakTodayTasks(context: Context) {
        val prefs = context.getSharedPreferences("velox_prefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("access_token", null)

        if (accessToken == null) {
            runOnUi(context) {
                Toast.makeText(context, "Токен не найден. Войдите заново.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}/api/tasks/")
            .addHeader("Authorization", "JWT $accessToken")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUi(context) {
                    Toast.makeText(context, "Ошибка сети: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    runOnUi(context) {
                        Toast.makeText(context, "Ошибка сервера: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                try {
                    val jsonArray = JSONArray(body)
                    val today = LocalDate.now().toString()

                    val tasksToday = mutableListOf<String>()

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val date = item.optString("date")

                        if (date == today) {
                            val title = item.optString("title", "Без названия")
                            val time = item.optString("time_start", "")

                            val line = if (time.isNotBlank()) {
                                "${formatTimeForSpeech(time)} — $title"
                            } else {
                                title
                            }

                            tasksToday.add(line)
                        }
                    }

                    val textToRead = when {
                        tasksToday.isEmpty() ->
                            "На сегодня задач нет."
                        else ->
                            "Ваши задачи на сегодня: " + tasksToday.joinToString(". ")
                    }

                    // Озвучиваем уже из UI-потока
                    runOnUi(context) {
                        speakText(context, textToRead)
                    }

                } catch (e: Exception) {
                    runOnUi(context) {
                        Toast.makeText(context, "Ошибка обработки задач", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun speakText(context: Context, text: String) {
        if (tts == null) {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {

                    val ru = Locale("ru", "RU")
                    val result = tts?.setLanguage(ru)

                    // Выбираем лучший голос
                    val voices = tts?.voices
                        ?.filter { it.locale == ru }
                        ?.sortedByDescending { v ->
                            // голоса с нейронными моделями отмечены флагами
                            (if (v.isNetworkConnectionRequired) 1 else 0) +
                                    (if (v.quality == Voice.QUALITY_HIGH) 10 else 0)
                        }

                    if (!voices.isNullOrEmpty()) {
                        tts?.voice = voices.first() // лучший голос
                    }

                    // Скорость и тон (можешь менять)
                    tts?.setPitch(1.0f)
                    tts?.setSpeechRate(1.0f)

                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "velox_tts")
                } else {
                    Toast.makeText(context, "Ошибка инициализации голоса", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "velox_tts")
        }
    }


    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

/**
 * Удобный wrapper, чтобы в Compose вызывать просто speakTodayTasks(context)
 */
@RequiresApi(Build.VERSION_CODES.O)
fun speakTodayTasks(context: Context) {
    VoiceAssistant.speakTodayTasks(context)
}
