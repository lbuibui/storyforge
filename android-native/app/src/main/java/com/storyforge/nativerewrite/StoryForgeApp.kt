package com.storyforge.nativerewrite

import android.app.Application
import com.storyforge.nativerewrite.di.AppGraph

class StoryForgeApp : Application() {
    lateinit var graph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        graph = AppGraph(this)
    }
}
