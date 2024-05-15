package com.jetbrains.rider.plugins.meadow.configurations

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rdclient.util.idea.pumpMessages
import com.jetbrains.rider.deploy.RiderDeploymentHost
import com.jetbrains.rider.meadow.generated.MeadowDeploymentArgs
import com.jetbrains.rider.meadow.generated.MeadowDeploymentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async

@OptIn(ExperimentalCoroutinesApi::class)
fun deploySync(executable: MeadowExecutable, debug: Boolean, project: Project) : MeadowDeploymentResult {
    val deploymentResultDeferred = project.service<MeadowLifetimeService>().scope.async(Dispatchers.Main) {
        deploy(executable, debug, project)
    }

    pumpMessages { deploymentResultDeferred.isCompleted }

    return deploymentResultDeferred.getCompleted()
}

suspend fun deploy(executable: MeadowExecutable, debug: Boolean, project: Project): MeadowDeploymentResult {
    return RiderDeploymentHost.getInstance(project).deployWithProgress(
        MeadowDeploymentArgs(
            executable.device.port,
            executable.appPath.absolutePath,
            debug,
            executable.runnableProject.kind,
            executable.projectFilePath)
    )
}
