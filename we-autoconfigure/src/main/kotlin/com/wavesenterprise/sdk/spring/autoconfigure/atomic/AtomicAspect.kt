package com.wavesenterprise.sdk.spring.autoconfigure.atomic

import com.wavesenterprise.sdk.atomic.AtomicBroadcaster
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut

@Aspect
class AtomicAspect(
    private val atomicBroadcaster: AtomicBroadcaster
) {

    @Pointcut("@annotation(com.wavesenterprise.sdk.spring.autoconfigure.atomic.annotation.Atomic)")
    fun atomic() {}

    @Around("atomic()")
    fun aroundAtomic(joinPoint: ProceedingJoinPoint): Any? =
        atomicBroadcaster.doInAtomic {
            joinPoint.proceed()
        }
}
