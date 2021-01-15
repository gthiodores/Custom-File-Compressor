import com.marvinjason.huffmancoding.HuffmanCoding
import loggersoft.kotlin.streams.BitStream
import loggersoft.kotlin.streams.openBinaryStream
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.awt.image.MemoryImageSource
import java.awt.image.PixelGrabber
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import javax.imageio.ImageIO

fun main() {
    var inputCode: Int? = 0

    while (inputCode != 3) {
        println(
            """
        1. Kompresi
        2. Buka kompresi
        3. Exit
    """.trimIndent()
        )
        print("Pilihan anda : ")
        inputCode = readLine()?.toIntOrNull()
        println()

        mainMenu(inputCode)
    }
}

private fun mainMenu(option: Int?) {
    when (option) {
        1 -> compression()
        2 -> decompress()
        3 -> println("Selamat tinggal")
        else -> println("Pilihan tersebut tidak tersedia")
    }
}

private fun compression() {
    divider()
    println("Anda masuk menu kompresi data")

    print("Masukkan path file image yang ingin dikompresi : ")
    val imagePath = readLine()

    when (imagePath) {
        null -> println("Path tidak boleh kosong")
        else -> imageCompression(imagePath)
    }
}

private fun imageCompression(imagePath: String) {
    val image = ImageIO.read(File(imagePath))
    val width = image.width
    val height = image.height

    val pixels = IntArray(width * height)
    PixelGrabber(image, 0, 0, width, height, pixels, 0, width).run {
        grabPixels()
    }

    print("Masukkan path file hasil kompresi: ")
    val filePath = readLine()

    when (filePath) {
        null -> println("Path file tidak boleh null")
        else -> {
            val transformResult = transformImage(pixels, width)
            writeTextToFile(filePath, transformResult)
        }
    }
}

// Transforms an array of integers into a haar transformed 2d matrix equivalent
private fun transformImage(imageData: IntArray, width: Int): Array<Array<Double>> {
    val newArray = mutableListOf<Array<Double>>()
    val tempArray = MutableList (width) {
        it.toDouble()
    }
    var counter = 0

    imageData.forEach {
        if (counter >= width) {
            counter = 0
            newArray.add(tempArray.toTypedArray())
        }

        tempArray[counter] = it.toDouble()
        counter++
    }
    newArray.add(tempArray.toTypedArray())

    return HaarTransformation().twoDimensionalHaar(newArray.toTypedArray())
}

// Writes image data after compressing it with huffmancoding
fun writeTextToFile(path: String, imageData: Array<Array<Double>>) {
    var stringBuilder = ""

    for (i in imageData.indices) {
        for (j in imageData[i].indices) {
            val s = imageData[i][j]
            stringBuilder += "$s, "
        }
        if (i < imageData.size.minus(1)) stringBuilder += "|"
        else {
            stringBuilder = stringBuilder.dropLast(2)
        }
    }

    val huffmanCoding = HuffmanCoding(stringBuilder).apply {
        compress()
    }
    val codeTree = StringBuilder()
    huffmanCoding.dictionary.forEach {
        codeTree.append("${it.key}:${it.value};")
    }

    writeToFile(
        File(path),
        huffmanCoding.compressedString,
        saveAsBinaryFile = true,
        codeTree = codeTree.toString()
    )
}

fun writeToFile(file: File, text: String, saveAsBinaryFile: Boolean = false, codeTree: String = "") {
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()

    if (saveAsBinaryFile) {
        file.writeText("${text.length}\n$codeTree\n")
        BitStream(file.openBinaryStream(false)).use { stream ->
            stream.position = stream.size
            text.forEach {
                stream += it == '1'
            }
            stream.close()
        }
    } else file.writeText(text)
}

private fun decompress() {
    divider()
    println("Anda masuk menu buka kompresi data")

    print("Masukkan path file yang ingin dibuka kompresi : ")
    val filePath = readLine()

    when (filePath) {
        null -> println("File path tidak boleh kosong")
        else -> decompressImage(filePath)
    }
}

private fun decompressImage(filePath: String) {
    val pixelAndWidth = readTextFromFile(filePath)

    print("Masukkan path hasil buka kompresi: ")
    val resultDir = readLine()

    when (resultDir) {
        null -> println("Path hasil buka kompresi tidak boleh kosong")
        else -> textToImage(resultDir,pixelAndWidth.second, pixelAndWidth.second, pixelAndWidth.first)
    }
}

// Converts text file containing integer arrays in strings into integer arrays
fun readTextFromFile(path: String): Pair<IntArray, Int> {
    println("Processing text file...")

    val uncompressedResult = readFromBinaryFile(path).getUncompressedString()

    val tempPath = path.substringBeforeLast('/')

    writeToFile(File("$tempPath/temp.txt"), uncompressedResult, false)

    val csv = BufferedReader(FileReader("$tempPath/temp.txt"))

    val resultArray = parseCSV(csv)

    if (File("$tempPath/temp.txt").exists()) {
        File("$tempPath/temp.txt").delete()
    }

    return resultArray
}

//Converts csv format into an int array
fun parseCSV(csv: BufferedReader): Pair<IntArray, Int> {
    val fileData = mutableListOf<String>()
    var row = csv.readLine()
    row = row.replace("|", "\n")

    fileData.addAll(row.split("\n"))

    val mutableArray = mutableListOf<Array<Double>>()

    fileData.forEach {
        val tempInt = mutableListOf<Double>()
        val tempList = mutableListOf<String>()
        tempList.addAll(it.split(", "))

        tempList.forEach {
            if (it != "")
                tempInt.add(it.toDouble())
        }
        mutableArray.add(tempInt.toTypedArray())
    }

    val resultArray = HaarTransformation().inverse2DHaar(mutableArray.toTypedArray())

    val tempList = mutableListOf<Int>()
    resultArray.forEach {array ->
        array.forEach { item ->
            tempList.add(item.toInt())
        }
    }

    val resultIntArray = IntArray(tempList.size)
    for (i in tempList.indices) {
        resultIntArray.set(i, tempList[i])
    }

    return Pair(resultIntArray, mutableArray[0].size)
}

// Create image from array of integers
fun textToImage(path: String, width: Int, height: Int, imageArray: IntArray) {
    val memoryImage = MemoryImageSource(width, height, imageArray, 0, width)
    val image = Toolkit.getDefaultToolkit().createImage(memoryImage)

    val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    bufferedImage.graphics.drawImage(image, 0, 0, null)
    ImageIO.write(bufferedImage, "jpg", File(path))
    println("Done!, Check your image")
}

fun readFromBinaryFile(path: String): UncompressedResult {
    val file = File(path)
    var result = ""
    var sizeBits = ""
    var codeTree = ""

    if (file.exists()) {
        val lines = file.bufferedReader().readLines()
        val streamSize = lines[0]
        codeTree = lines[1]

        val splitPath = path.substringBeforeLast('/')

        val tempFile = File("$splitPath/uncompressed.txt")
        writeToFile(tempFile, "$streamSize\n$codeTree\n")

        BitStream(tempFile.openBinaryStream(false)).use { stream ->
            while (stream.position < stream.size) {
                sizeBits += when (stream.readBit()) {
                    false -> 0
                    true -> 1
                }
            }
            stream.close()
        }
        tempFile.delete()

        BitStream(file.openBinaryStream(false)).use { stream ->
            stream.skip(sizeBits.length)
            while (stream.position < stream.size) {
                result += when (stream.readBit()) {
                    false -> 0
                    true -> 1
                }
            }
            stream.close()
        }

        result = result.take(streamSize.toInt())
    }

    return UncompressedResult(result, codeTree)
}

private fun divider() {
    println("====================================================")
}