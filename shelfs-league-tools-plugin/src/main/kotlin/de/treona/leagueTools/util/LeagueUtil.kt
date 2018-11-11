package de.treona.leagueTools.util

class LeagueUtil {

    companion object {
        fun parseTierDivision(entryName: String): String{
            val parts = entryName.split("_")
            val stringBuilder = StringBuilder()
            stringBuilder.append(parts[0].substring(0, 1) + parts[0].substring(1, parts[0].length).toLowerCase())
            if(parts.size > 1){
                stringBuilder.append(" ").append(parts[1])
            }
            return stringBuilder.toString()
        }
    }
}