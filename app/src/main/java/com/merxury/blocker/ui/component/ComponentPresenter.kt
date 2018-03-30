package com.merxury.blocker.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ComponentInfo
import android.content.pm.PackageManager
import android.util.Log
import com.merxury.blocker.core.ApplicationComponents
import com.merxury.blocker.core.IController
import com.merxury.blocker.core.root.ComponentControllerProxy
import com.merxury.blocker.core.root.EControllerMethod
import com.merxury.blocker.entity.getSimpleName
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiConsumer
import io.reactivex.schedulers.Schedulers

class ComponentPresenter(val pm: PackageManager, val view: ComponentContract.View) : ComponentContract.Presenter, IController {

    private val controller: IController by lazy {
        ComponentControllerProxy.getInstance(EControllerMethod.PM, null)
    }

    init {
        view.presenter = this
    }

    @SuppressLint("CheckResult")
    override fun loadComponents(packageName: String, type: EComponentType) {
        Log.i(TAG, "Trying to load components for $packageName, type: $type")
        view.setLoadingIndicator(true)
        Single.create((SingleOnSubscribe<List<ComponentInfo>> { emitter ->
            var componentList = when (type) {
                EComponentType.RECEIVER -> ApplicationComponents.getReceiverList(pm, packageName)
                EComponentType.ACTIVITY -> ApplicationComponents.getActivityList(pm, packageName)
                EComponentType.SERVICE -> ApplicationComponents.getServiceList(pm, packageName)
                EComponentType.PROVIDER -> ApplicationComponents.getProviderList(pm, packageName)
            }
            componentList = sortComponentList(componentList, currentComparator)
            emitter.onSuccess(componentList)
        })).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ components ->
                    view.setLoadingIndicator(false)
                    if (components.isEmpty()) {
                        view.showNoComponent()
                    } else {
                        view.showComponentList(components)
                    }
                })
    }

    @SuppressLint("CheckResult")
    override fun switchComponent(packageName: String, componentName: String, state: Int): Boolean {
        Single.create((SingleOnSubscribe<Boolean> { emitter ->
            try {
                val result = controller.switchComponent(packageName, componentName, state)
                emitter.onSuccess(result)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        })).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(BiConsumer { _, error ->
                    view.refreshComponentSwitchState(componentName)
                    error?.apply {
                        Log.e(TAG, message)
                        printStackTrace()
                        view.showAlertDialog()
                    }
                })
        return true
    }

    @SuppressLint("CheckResult")
    override fun enableComponent(componentInfo: ComponentInfo): Boolean {
        Log.i(TAG, "Trying to enable component: ${componentInfo.name}")
        Single.create((SingleOnSubscribe<Boolean> { emitter ->
            try {
                val result = controller.enableComponent(componentInfo)
                emitter.onSuccess(result)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        })).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(BiConsumer { _, error ->
                    view.refreshComponentSwitchState(componentInfo.name)
                    error?.apply {
                        Log.e(TAG, message)
                        printStackTrace()
                        view.showAlertDialog()
                    }
                })
        return true
    }

    @SuppressLint("CheckResult")
    override fun disableComponent(componentInfo: ComponentInfo): Boolean {
        Log.i(TAG, "Trying to disable component: ${componentInfo.name}")
        Single.create((SingleOnSubscribe<Boolean> { emitter ->
            try {
                val result = controller.disableComponent(componentInfo)
                emitter.onSuccess(result)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        })).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(BiConsumer { _, error ->
                    view.refreshComponentSwitchState(componentInfo.name)
                    error?.apply {
                        Log.e(TAG, message)
                        printStackTrace()
                        view.showAlertDialog()
                    }
                })
        return true
    }

    override fun sortComponentList(components: List<ComponentInfo>, type: EComponentComparatorType): List<ComponentInfo> {
        return when (type) {
            EComponentComparatorType.NAME_ASCENDING -> components.sortedBy { it.getSimpleName() }
            EComponentComparatorType.NAME_DESCENDING -> components.sortedByDescending { it.getSimpleName() }
            EComponentComparatorType.PACKAGE_NAME_ASCENDING -> components.sortedBy { it.name }
            EComponentComparatorType.PACKAGE_NAME_DESCENDING -> components.sortedByDescending { it.name }
        }
    }

    override fun start(context: Context) {
    }

    override var currentComparator: EComponentComparatorType = EComponentComparatorType.NAME_ASCENDING

    companion object {
        const val TAG = "ComponentPresenter"
    }
}