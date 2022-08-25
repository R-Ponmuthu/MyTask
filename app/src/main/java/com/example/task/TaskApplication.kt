package com.example.task

import android.app.Application

import dagger.hilt.android.HiltAndroidApp

// Required for Hilt dependency injection
@HiltAndroidApp
class TaskApplication : Application()
