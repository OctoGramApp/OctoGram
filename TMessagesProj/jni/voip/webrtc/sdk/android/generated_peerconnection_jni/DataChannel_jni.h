// Copyright 2014 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.


// This file is autogenerated by
//     third_party/jni_zero/jni_generator.py
// For
//     org/webrtc/DataChannel

#ifndef org_webrtc_DataChannel_JNI
#define org_webrtc_DataChannel_JNI

#include <jni.h>

#include "third_party/jni_zero/jni_export.h"
#include "webrtc/sdk/android/src/jni/jni_generator_helper.h"


// Step 1: Forward declarations.

JNI_ZERO_COMPONENT_BUILD_EXPORT extern const char kClassPath_org_webrtc_DataChannel[];
const char kClassPath_org_webrtc_DataChannel[] = "org/webrtc/DataChannel";

JNI_ZERO_COMPONENT_BUILD_EXPORT extern const char kClassPath_org_webrtc_DataChannel_00024Buffer[];
const char kClassPath_org_webrtc_DataChannel_00024Buffer[] = "org/webrtc/DataChannel$Buffer";

JNI_ZERO_COMPONENT_BUILD_EXPORT extern const char kClassPath_org_webrtc_DataChannel_00024Init[];
const char kClassPath_org_webrtc_DataChannel_00024Init[] = "org/webrtc/DataChannel$Init";

JNI_ZERO_COMPONENT_BUILD_EXPORT extern const char kClassPath_org_webrtc_DataChannel_00024Observer[];
const char kClassPath_org_webrtc_DataChannel_00024Observer[] = "org/webrtc/DataChannel$Observer";

JNI_ZERO_COMPONENT_BUILD_EXPORT extern const char kClassPath_org_webrtc_DataChannel_00024State[];
const char kClassPath_org_webrtc_DataChannel_00024State[] = "org/webrtc/DataChannel$State";
// Leaking this jclass as we cannot use LazyInstance from some threads.
JNI_ZERO_COMPONENT_BUILD_EXPORT std::atomic<jclass> g_org_webrtc_DataChannel_clazz(nullptr);
#ifndef org_webrtc_DataChannel_clazz_defined
#define org_webrtc_DataChannel_clazz_defined
inline jclass org_webrtc_DataChannel_clazz(JNIEnv* env) {
  return jni_zero::LazyGetClass(env, kClassPath_org_webrtc_DataChannel,
      &g_org_webrtc_DataChannel_clazz);
}
#endif
// Leaking this jclass as we cannot use LazyInstance from some threads.
JNI_ZERO_COMPONENT_BUILD_EXPORT std::atomic<jclass>
    g_org_webrtc_DataChannel_00024Buffer_clazz(nullptr);
#ifndef org_webrtc_DataChannel_00024Buffer_clazz_defined
#define org_webrtc_DataChannel_00024Buffer_clazz_defined
inline jclass org_webrtc_DataChannel_00024Buffer_clazz(JNIEnv* env) {
  return jni_zero::LazyGetClass(env, kClassPath_org_webrtc_DataChannel_00024Buffer,
      &g_org_webrtc_DataChannel_00024Buffer_clazz);
}
#endif
// Leaking this jclass as we cannot use LazyInstance from some threads.
JNI_ZERO_COMPONENT_BUILD_EXPORT std::atomic<jclass>
    g_org_webrtc_DataChannel_00024Init_clazz(nullptr);
#ifndef org_webrtc_DataChannel_00024Init_clazz_defined
#define org_webrtc_DataChannel_00024Init_clazz_defined
inline jclass org_webrtc_DataChannel_00024Init_clazz(JNIEnv* env) {
  return jni_zero::LazyGetClass(env, kClassPath_org_webrtc_DataChannel_00024Init,
      &g_org_webrtc_DataChannel_00024Init_clazz);
}
#endif
// Leaking this jclass as we cannot use LazyInstance from some threads.
JNI_ZERO_COMPONENT_BUILD_EXPORT std::atomic<jclass>
    g_org_webrtc_DataChannel_00024Observer_clazz(nullptr);
#ifndef org_webrtc_DataChannel_00024Observer_clazz_defined
#define org_webrtc_DataChannel_00024Observer_clazz_defined
inline jclass org_webrtc_DataChannel_00024Observer_clazz(JNIEnv* env) {
  return jni_zero::LazyGetClass(env, kClassPath_org_webrtc_DataChannel_00024Observer,
      &g_org_webrtc_DataChannel_00024Observer_clazz);
}
#endif
// Leaking this jclass as we cannot use LazyInstance from some threads.
JNI_ZERO_COMPONENT_BUILD_EXPORT std::atomic<jclass>
    g_org_webrtc_DataChannel_00024State_clazz(nullptr);
#ifndef org_webrtc_DataChannel_00024State_clazz_defined
#define org_webrtc_DataChannel_00024State_clazz_defined
inline jclass org_webrtc_DataChannel_00024State_clazz(JNIEnv* env) {
  return jni_zero::LazyGetClass(env, kClassPath_org_webrtc_DataChannel_00024State,
      &g_org_webrtc_DataChannel_00024State_clazz);
}
#endif


// Step 2: Constants (optional).


// Step 3: Method stubs.
namespace webrtc {
namespace jni {

static jlong JNI_DataChannel_BufferedAmount(JNIEnv* env, const jni_zero::JavaParamRef<jobject>&
    jcaller);

JNI_BOUNDARY_EXPORT jlong Java_org_webrtc_DataChannel_nativeBufferedAmount(
    JNIEnv* env,
    jobject jcaller) {
  return JNI_DataChannel_BufferedAmount(env, jni_zero::JavaParamRef<jobject>(env, jcaller));
}

static void JNI_DataChannel_Close(JNIEnv* env, const jni_zero::JavaParamRef<jobject>& jcaller);

JNI_BOUNDARY_EXPORT void Java_org_webrtc_DataChannel_nativeClose(
    JNIEnv* env,
    jobject jcaller) {
  return JNI_DataChannel_Close(env, jni_zero::JavaParamRef<jobject>(env, jcaller));
}

static jint JNI_DataChannel_Id(JNIEnv* env, const jni_zero::JavaParamRef<jobject>& jcaller);

JNI_BOUNDARY_EXPORT jint Java_org_webrtc_DataChannel_nativeId(
    JNIEnv* env,
    jobject jcaller) {
  return JNI_DataChannel_Id(env, jni_zero::JavaParamRef<jobject>(env, jcaller));
}

static jni_zero::ScopedJavaLocalRef<jstring> JNI_DataChannel_Label(JNIEnv* env, const
    jni_zero::JavaParamRef<jobject>& jcaller);

JNI_BOUNDARY_EXPORT jstring Java_org_webrtc_DataChannel_nativeLabel(
    JNIEnv* env,
    jobject jcaller) {
  return JNI_DataChannel_Label(env, jni_zero::JavaParamRef<jobject>(env, jcaller)).Release();
}

static jlong JNI_DataChannel_RegisterObserver(JNIEnv* env, const jni_zero::JavaParamRef<jobject>&
    jcaller,
    const jni_zero::JavaParamRef<jobject>& observer);

JNI_BOUNDARY_EXPORT jlong Java_org_webrtc_DataChannel_nativeRegisterObserver(
    JNIEnv* env,
    jobject jcaller,
    jobject observer) {
  return JNI_DataChannel_RegisterObserver(env, jni_zero::JavaParamRef<jobject>(env, jcaller),
      jni_zero::JavaParamRef<jobject>(env, observer));
}

static jboolean JNI_DataChannel_Send(JNIEnv* env, const jni_zero::JavaParamRef<jobject>& jcaller,
    const jni_zero::JavaParamRef<jbyteArray>& data,
    jboolean binary);

JNI_BOUNDARY_EXPORT jboolean Java_org_webrtc_DataChannel_nativeSend(
    JNIEnv* env,
    jobject jcaller,
    jbyteArray data,
    jboolean binary) {
  return JNI_DataChannel_Send(env, jni_zero::JavaParamRef<jobject>(env, jcaller),
      jni_zero::JavaParamRef<jbyteArray>(env, data), binary);
}

static jni_zero::ScopedJavaLocalRef<jobject> JNI_DataChannel_State(JNIEnv* env, const
    jni_zero::JavaParamRef<jobject>& jcaller);

JNI_BOUNDARY_EXPORT jobject Java_org_webrtc_DataChannel_nativeState(
    JNIEnv* env,
    jobject jcaller) {
  return JNI_DataChannel_State(env, jni_zero::JavaParamRef<jobject>(env, jcaller)).Release();
}

static void JNI_DataChannel_UnregisterObserver(JNIEnv* env, const jni_zero::JavaParamRef<jobject>&
    jcaller,
    jlong observer);

JNI_BOUNDARY_EXPORT void Java_org_webrtc_DataChannel_nativeUnregisterObserver(
    JNIEnv* env,
    jobject jcaller,
    jlong observer) {
  return JNI_DataChannel_UnregisterObserver(env, jni_zero::JavaParamRef<jobject>(env, jcaller),
      observer);
}


static std::atomic<jmethodID> g_org_webrtc_DataChannel_Constructor1(nullptr);
static jni_zero::ScopedJavaLocalRef<jobject> Java_DataChannel_Constructor(JNIEnv* env, jlong
    nativeDataChannel) {
  jclass clazz = org_webrtc_DataChannel_clazz(env);
  CHECK_CLAZZ(env, clazz,
      org_webrtc_DataChannel_clazz(env), nullptr);

  jni_zero::JniJavaCallContextChecked call_context;
  call_context.Init<
      jni_zero::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "<init>",
          "(J)V",
          &g_org_webrtc_DataChannel_Constructor1);

  jobject ret =
      env->NewObject(clazz,
          call_context.base.method_id, nativeDataChannel);
  return jni_zero::ScopedJavaLocalRef<jobject>(env, ret);
}

static std::atomic<jmethodID> g_org_webrtc_DataChannel_getNativeDataChannel0(nullptr);
static jlong Java_DataChannel_getNativeDataChannel(JNIEnv* env, const jni_zero::JavaRef<jobject>&
    obj) {
  jclass clazz = org_webrtc_DataChannel_clazz(env);
  CHECK_CLAZZ(env, obj.obj(),
      org_webrtc_DataChannel_clazz(env), 0);

  jni_zero::JniJavaCallContextChecked call_context;
  call_context.Init<
      jni_zero::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "getNativeDataChannel",
          "()J",
          &g_org_webrtc_DataChannel_getNativeDataChannel0);

  jlong ret =
      env->CallLongMethod(obj.obj(),
          call_context.base.method_id);
  return ret;
}

static std::atomic<jmethodID> g_org_webrtc_DataChannel_00024Buffer_Constructor2(nullptr);
static jni_zero::ScopedJavaLocalRef<jobject> Java_Buffer_Constructor(JNIEnv* env, const
    jni_zero::JavaRef<jobject>& data,
    jboolean binary) {
  jclass clazz = org_webrtc_DataChannel_00024Buffer_clazz(env);
  CHECK_CLAZZ(env, clazz,
      org_webrtc_DataChannel_00024Buffer_clazz(env), nullptr);

  jni_zero::JniJavaCallContextChecked call_context;
  call_context.Init<
      jni_zero::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "<init>",
          "(Ljava/nio/ByteBuffer;Z)V",
          &g_org_webrtc_DataChannel_00024Buffer_Constructor2);

  jobject ret =
      env->NewObject(clazz,
          call_context.base.method_id, data.obj(), binary);
  return jni_zero::ScopedJavaLocalRef<jobject>(env, ret);
}

static std::atomic<jmethodID> g_org_webrtc_DataChannel_00024Init_getId0(nullptr);
static jint Java_Init_getId(JNIEnv* env, const jni_zero::JavaRef<jobject>& obj) {
  jclass clazz = org_webrtc_DataChannel_00024Init_clazz(env);
  CHECK_CLAZZ(env, obj.obj(),
      org_webrtc_DataChannel_00024Init_clazz(env), 0);

  jni_zero::JniJavaCallContextChecked call_context;
  call_context.Init<
      jni_zero::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "getId",
          "()I",
          &g_org_webrtc_DataChannel_00024Init_getId0);

  jint ret =
      env->CallIntMethod(obj.obj(),
          call_context.base.method_id);
  return ret;
}

static std::atomic<jmethodID> g_org_webrtc_DataChannel_00024Init_getMaxRetransmitTimeMs0(nullptr);
static jint Java_Init_getMaxRetransmitTimeMs(JNIEnv* env, const jni_zero::JavaRef<jobject>& obj) {
  jclass clazz = org_webrtc_DataChannel_00024Init_clazz(env);
  CHECK_CLAZZ(env, obj.obj(),
      org_webrtc_DataChannel_00024Init_clazz(env), 0);

  jni_zero::JniJavaCallContextChecked call_context;
  call_context.Init<
      jni_zero::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "getMaxRetransmitTimeMs",
          "()I",
          &g_org_webrtc_DataChannel_00024Init_getMaxRetransmitTimeMs0);

  jint ret =
      env->CallIntMethod(obj.obj(),
          call_context.base.method_id);
  return ret;
}

static std::atomic<jmethodID> g_org_webrtc_DataChannel_00024Init_getMaxRetransmits0(nullptr);
static jint Java_Init_getMaxRetransmits(JNIEnv* env, const jni_zero::JavaRef<jobject>& obj) {
  jclass clazz = org_webrtc_DataChannel_00024Init_clazz(env);
  CHECK_CLAZZ(env, obj.obj(),
      org_webrtc_DataChannel_00024Init_clazz(env), 0);

  jni_zero::JniJavaCallContextChecked call_context;
  call_context.Init<
      jni_zero::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "getMaxRetransmits",
          "()I",
          &g_org_webrtc_DataChannel_00024Init_getMaxRetransmits0);

  jint ret =
      env->CallIntMethod(obj.obj(),
          call_context.base.method_id);
  return ret;
}

static std::atomic<jmethodID> g_org_webrtc_DataChannel_00024Init_getNegotiated0(nullptr);
static jboolean Java_Init_getNegotiated(JNIEnv* env, const jni_zero::JavaRef<jobject>& obj) {
  jclass clazz = org_webrtc_DataChannel_00024Init_clazz(env);
  CHECK_CLAZZ(env, obj.obj(),
      org_webrtc_DataChannel_00024Init_clazz(env), false);

  jni_zero::JniJavaCallContextChecked call_context;
  call_context.Init<
      jni_zero::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "getNegotiated",
          "()Z",
          &g_org_webrtc_DataChannel_00024Init_getNegotiated0);

  jboolean ret =
      env->CallBooleanMethod(obj.obj(),
          call_context.base.method_id);
  return ret;
}

static std::atomic<jmethodID> g_org_webrtc_DataChannel_00024Init_getOrdered0(nullptr);
static jboolean Java_Init_getOrdered(JNIEnv* env, const jni_zero::JavaRef<jobject>& obj) {
  jclass clazz = org_webrtc_DataChannel_00024Init_clazz(env);
  CHECK_CLAZZ(env, obj.obj(),
      org_webrtc_DataChannel_00024Init_clazz(env), false);

  jni_zero::JniJavaCallContextChecked call_context;
  call_context.Init<
      jni_zero::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "getOrdered",
          "()Z",
          &g_org_webrtc_DataChannel_00024Init_getOrdered0);

  jboolean ret =
      env->CallBooleanMethod(obj.obj(),
          call_context.base.method_id);
  return ret;
}

static std::atomic<jmethodID> g_org_webrtc_DataChannel_00024Init_getProtocol0(nullptr);
static jni_zero::ScopedJavaLocalRef<jstring> Java_Init_getProtocol(JNIEnv* env, const
    jni_zero::JavaRef<jobject>& obj) {
  jclass clazz = org_webrtc_DataChannel_00024Init_clazz(env);
  CHECK_CLAZZ(env, obj.obj(),
      org_webrtc_DataChannel_00024Init_clazz(env), nullptr);

  jni_zero::JniJavaCallContextChecked call_context;
  call_context.Init<
      jni_zero::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "getProtocol",
          "()Ljava/lang/String;",
          &g_org_webrtc_DataChannel_00024Init_getProtocol0);

  jstring ret =
      static_cast<jstring>(env->CallObjectMethod(obj.obj(),
          call_context.base.method_id));
  return jni_zero::ScopedJavaLocalRef<jstring>(env, ret);
}

static std::atomic<jmethodID>
    g_org_webrtc_DataChannel_00024Observer_onBufferedAmountChange1(nullptr);
static void Java_Observer_onBufferedAmountChange(JNIEnv* env, const jni_zero::JavaRef<jobject>& obj,
    jlong previousAmount) {
  jclass clazz = org_webrtc_DataChannel_00024Observer_clazz(env);
  CHECK_CLAZZ(env, obj.obj(),
      org_webrtc_DataChannel_00024Observer_clazz(env));

  jni_zero::JniJavaCallContextChecked call_context;
  call_context.Init<
      jni_zero::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "onBufferedAmountChange",
          "(J)V",
          &g_org_webrtc_DataChannel_00024Observer_onBufferedAmountChange1);

     env->CallVoidMethod(obj.obj(),
          call_context.base.method_id, previousAmount);
}

static std::atomic<jmethodID> g_org_webrtc_DataChannel_00024Observer_onMessage1(nullptr);
static void Java_Observer_onMessage(JNIEnv* env, const jni_zero::JavaRef<jobject>& obj, const
    jni_zero::JavaRef<jobject>& buffer) {
  jclass clazz = org_webrtc_DataChannel_00024Observer_clazz(env);
  CHECK_CLAZZ(env, obj.obj(),
      org_webrtc_DataChannel_00024Observer_clazz(env));

  jni_zero::JniJavaCallContextChecked call_context;
  call_context.Init<
      jni_zero::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "onMessage",
          "(Lorg/webrtc/DataChannel$Buffer;)V",
          &g_org_webrtc_DataChannel_00024Observer_onMessage1);

     env->CallVoidMethod(obj.obj(),
          call_context.base.method_id, buffer.obj());
}

static std::atomic<jmethodID> g_org_webrtc_DataChannel_00024Observer_onStateChange0(nullptr);
static void Java_Observer_onStateChange(JNIEnv* env, const jni_zero::JavaRef<jobject>& obj) {
  jclass clazz = org_webrtc_DataChannel_00024Observer_clazz(env);
  CHECK_CLAZZ(env, obj.obj(),
      org_webrtc_DataChannel_00024Observer_clazz(env));

  jni_zero::JniJavaCallContextChecked call_context;
  call_context.Init<
      jni_zero::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "onStateChange",
          "()V",
          &g_org_webrtc_DataChannel_00024Observer_onStateChange0);

     env->CallVoidMethod(obj.obj(),
          call_context.base.method_id);
}

static std::atomic<jmethodID> g_org_webrtc_DataChannel_00024State_fromNativeIndex1(nullptr);
static jni_zero::ScopedJavaLocalRef<jobject> Java_State_fromNativeIndex(JNIEnv* env, JniIntWrapper
    nativeIndex) {
  jclass clazz = org_webrtc_DataChannel_00024State_clazz(env);
  CHECK_CLAZZ(env, clazz,
      org_webrtc_DataChannel_00024State_clazz(env), nullptr);

  jni_zero::JniJavaCallContextChecked call_context;
  call_context.Init<
      jni_zero::MethodID::TYPE_STATIC>(
          env,
          clazz,
          "fromNativeIndex",
          "(I)Lorg/webrtc/DataChannel$State;",
          &g_org_webrtc_DataChannel_00024State_fromNativeIndex1);

  jobject ret =
      env->CallStaticObjectMethod(clazz,
          call_context.base.method_id, as_jint(nativeIndex));
  return jni_zero::ScopedJavaLocalRef<jobject>(env, ret);
}

}  // namespace jni
}  // namespace webrtc

#endif  // org_webrtc_DataChannel_JNI
