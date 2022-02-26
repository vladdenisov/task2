import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermissions
import java.text.CharacterIterator
import java.text.SimpleDateFormat
import java.text.StringCharacterIterator
import java.util.*


fun displayPermissions(file: File, numeric: Boolean? = false): String {
    val permissions = PosixFilePermissions.toString(Files.getPosixFilePermissions(file.toPath()))
    if (numeric == true) {
        val result = StringBuilder()
        var current = 0
        for (i in permissions.indices) {

            if (permissions[i] == 'r') {
                current += 4
            }
            if (permissions[i] == 'w') {
                current += 2
            }
            if (permissions[i] == 'x') {
                current += 1
            }
            if ((i + 1) % 3 == 0) {
                result.append(current)
                current = 0
            }
        }
        return result.toString()
    }
    return if (file.isDirectory) {
        "d$permissions"
    } else {
        "-$permissions"
    }
}

fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val dateFormatter = SimpleDateFormat("MMM dd HH:mm")
    return dateFormatter.format(date)
}

fun humanReadableByteCountSI(byte: Long): String {
    var bytes = byte
    if (-1000 < bytes && bytes < 1000) {
        return "$bytes B"
    }
    val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
    while (bytes <= -999950 || bytes >= 999950) {
        bytes /= 1000
        ci.next()
    }
    return String.format("%.1f %cB", bytes / 1000.0, ci.current())
}

fun displayMeta(file: File, isLong: Boolean, isHuman: Boolean): String {
    val result = StringBuilder()
    if (isLong || isHuman) {
        result.append("${displayPermissions(file, isHuman)} ")
    }
    if (isLong) {
        result.append("${formatTime(file.lastModified())} ")
    }
    result.append(file.name)
    if (isLong) {
        result.append(" ${file.length()} B")
    }
    if (isHuman) {
        result.append(" ${humanReadableByteCountSI(file.length())}")
    }
    return result.toString()
}

fun generateOutput(path: String, isLong: Boolean, isHuman: Boolean, isReversed: Boolean): List<String> {
    var files = File(path).listFiles()?.sorted()!!
    if (isReversed)
        files = files.reversed()
    val strings = mutableListOf<String>()
    for (file in files) {
        strings.add(displayMeta(file, isLong, isHuman))
    }
    return strings
}


fun main(args: Array<String>) {
    val flags = mutableMapOf<Char, Boolean>(
        'h' to false,
        'l' to false,
        'r' to false,
        'o' to false,
    )
    for (i in args.indices) {
        if (args[i].contains('-') && args[i].length == 2) {
            if (args[i][1] in flags) {
                flags[args[i][1]] = true
            }
        }
    }
    val path = Paths.get("").toAbsolutePath().toString()
    val strings = generateOutput(path, flags.getOrDefault('l', false), flags.getOrDefault('h', false), flags.getOrDefault('r', false))
    for (string in strings) {
        println(string)
    }
}