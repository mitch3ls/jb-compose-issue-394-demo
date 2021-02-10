package at.cdfz.jsonsplitter.controller

sealed class ProcessingState {
    object Init : ProcessingState()
    object Error : ProcessingState()
    class Progress(val progress: Float) : ProcessingState()
    object Done : ProcessingState()
}