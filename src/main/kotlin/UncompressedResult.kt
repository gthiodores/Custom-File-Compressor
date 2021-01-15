class UncompressedResult(val compressedText: String = "", codeTree: String = "") {
    private val mappedTree: Map<String, String>

    init {
        val mutableList = mutableListOf<String>()
        mutableList.addAll(codeTree.split(';'))
        mutableList.removeAt(mutableList.lastIndex)

        val mutableMap = mutableMapOf<String, String>()
        mutableList.forEach {
            val splitString = it.split(":")
            mutableMap.put(splitString[1], splitString[0])
        }

        mappedTree = mutableMap.toMap()
    }

    fun getUncompressedString(): String {
        val result = StringBuilder()
        var temp = ""

        compressedText.forEach {
            temp += it
            if (mappedTree.containsKey(temp)) {
                result.append(mappedTree.getValue(temp))
                temp = ""
            }
        }

        return result.toString()
    }
}
