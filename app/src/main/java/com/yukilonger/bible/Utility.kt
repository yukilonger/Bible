package com.yukilonger.bible

import android.content.Context
import java.time.*
import java.time.format.DateTimeFormatter

class Utility {
    companion object {
        fun GetRandomIndex(maxIndex: Int): Int{
            return (0..maxIndex).random()
        }

        //
        // day: 2023-01-01
        fun IsHoliday(day: String): Boolean {
            return false
        }

        // 获取下个工作日
        fun GetNextWorkday(context: Context): LocalDate?{
            // 读取工作日文件
            val currentTime = LocalDate.now().toString().replace("-", "").toInt()
            context.resources.assets.open("workday").let {
                it.buffered().reader().use { reader ->
                    val lines: List<String> = reader.readLines()
                    lines.forEach { line ->
                        val temp = line.replace("-", "").toInt()
                        if(temp > currentTime) {
                            // 找到一个日期大于今天，则这个日期就是下个工作日
                            return LocalDate.parse(line, DateTimeFormatter.ISO_DATE)
                        }
                    }
                }
            }
            // 需要更新工作日文件
            return null
        }

        // 计算相差天数
        fun DiffDay(date: LocalDate): Int {
            return Duration.between(LocalDate.now(), date).toDays().toInt()
        }

        fun GetTimestamp(date: LocalDate): Long{
            return LocalDateTime.of(date, LocalTime.MIN).atZone(ZoneId.systemDefault()).toEpochSecond()
        }
    }
}