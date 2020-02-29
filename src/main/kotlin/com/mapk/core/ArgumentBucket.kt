package com.mapk.core

class ArgumentBucket(
    val bucket: Array<Any?>,
    private var initializeStatus: Int,
    private val initializeMask: List<Int>,
    // clone時の再計算を避けるため1回で済むようにデフォルト値化
    private val completionValue: Int = initializeMask.reduce { l, r -> l or r }
) : Cloneable {
    val isInitialized: Boolean get() = initializeStatus == completionValue
    val notInitializedParameterIndexes: List<Int> get() = initializeMask.indices.filter {
        initializeStatus and initializeMask[it] == 0
    }

    fun setArgument(argument: Any?, index: Int) {
        // 先に入ったものを優先するため、初期化済みなら何もしない
        if (initializeStatus and initializeMask[index] != 0) return

        bucket[index] = argument
        initializeStatus = initializeStatus or initializeMask[index]
    }

    public override fun clone(): ArgumentBucket {
        return ArgumentBucket(
            bucket.copyOf(),
            initializeStatus,
            initializeMask,
            completionValue
        )
    }
}
