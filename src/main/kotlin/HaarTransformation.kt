class HaarTransformation {

    fun oneDimensionalHaar(originalArray: Array<Double>): Array<Double> {
        val temporaryArray = mutableListOf<Double>().apply {
            this.addAll(originalArray)
        }

        var w = originalArray.size

        while (w != 1) {

            for (i in 0..w.div(2).minus(1)) {
                temporaryArray[i] = originalArray[i + i].plus(originalArray[i + i + 1]).div(2)
            }

            for ((k, j) in (w.div(2) until w).withIndex()) {
                temporaryArray[j] = originalArray[(j - (w.div(2).minus(1)) + k).minus(1)] - temporaryArray[k]
            }

            for (j in 0 until w) {
                originalArray[j] = temporaryArray[j]
            }

            w = w.div(2)
        }

        return originalArray
    }

    fun twoDimensionalHaar(originalArray: Array<Array<Double>>): Array<Array<Double>> {
        val temporaryArray = mutableListOf<Array<Double>>().apply {
            addAll(originalArray)
        }

        for (count in originalArray.indices) {
            originalArray[count] = oneDimensionalHaar(temporaryArray[count])
        }

        for (count in originalArray[0].indices) {
            val tempArray = mutableListOf<Double>()

            originalArray.forEach {
                tempArray.add(it[count])
            }

            val result = oneDimensionalHaar(tempArray.toTypedArray())

            for (i in result.indices) {
                originalArray[i][count] = result[i]
            }
        }

        return originalArray
    }

    fun inverse1DHaar(originalArray: Array<Double>): Array<Double> {
        val temporaryArray = mutableListOf<Double>().apply {
            addAll(originalArray)
        }

        var w = 1

        while (w < originalArray.size) {
            var n = w
            var m = 0

            for (i in 0..w - 1) {
                originalArray[m] = temporaryArray[i] + temporaryArray[n]
                originalArray[m + 1] = temporaryArray[i] - temporaryArray[n]

                n = n.plus(1)
                m = m.plus(2)
            }

            temporaryArray.clear()
            temporaryArray.addAll(originalArray)

            w = w.times(2)
        }

        return originalArray
    }

    fun inverse2DHaar(originalArray: Array<Array<Double>>): Array<Array<Double>> {
        val temporaryArray = mutableListOf<Array<Double>>().apply {
            addAll(originalArray)
        }

        for (count in originalArray[0].indices) {
            val tempArray = mutableListOf<Double>()

            originalArray.forEach {
                tempArray.add(it[count])
            }

            val result = inverse1DHaar(tempArray.toTypedArray())

            for (i in result.indices) {
                originalArray[i][count] = result[i]
            }
        }

        for (count in originalArray.indices) {
            originalArray[count] = inverse1DHaar(temporaryArray[count])
        }

        return originalArray
    }

}