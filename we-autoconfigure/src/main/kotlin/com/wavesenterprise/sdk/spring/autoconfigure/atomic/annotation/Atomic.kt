package com.wavesenterprise.sdk.spring.autoconfigure.atomic.annotation

import kotlin.annotation.AnnotationRetention.RUNTIME

@MustBeDocumented
@Retention(RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Atomic
