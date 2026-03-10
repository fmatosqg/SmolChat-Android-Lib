/*
 * Copyright (C) 2025 Shubham Panchal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.shubham0204.smollm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GGUFReader {
    companion object {
        init {
            System.loadLibrary("ggufreader")
        }
    }

    private var nativeHandle: Long = 0L

    suspend fun load(modelPath: String) {

        withContext(Dispatchers.IO) {
            nativeHandle = getGGUFContextNativeHandle(modelPath)
            assert(nativeHandle != 0L) { "Failed to load GGUF file: $modelPath" }

        }
    }

      suspend fun loadFromFd(fd: Int) =
          withContext(Dispatchers.IO) {
              nativeHandle = getGGUFContextNativeHandleFromFd(fd)

              assert(nativeHandle != 0L) { "Failed to load GGUF file from fd: $fd" }
          }

      suspend fun loadFromBuffer(buffer: java.nio.ByteBuffer) =
          withContext(Dispatchers.IO) {
              // Check magic bytes: GGUF (0x47 0x47 0x55 0x46)
              val magic = ByteArray(4)
              buffer.mark()
              buffer.get(magic)
              buffer.reset()
              val magicString = String(magic)
              assert(magicString == "GGUF") { 
                  if (magicString.startsWith("PK")) {
                      "Failed to load GGUF from buffer: The file is compressed (ZIP/APK). Ensure 'noCompress' is set in gradle for .gguf files."
                  } else {
                      "Failed to load GGUF from buffer: Invalid magic bytes '$magicString', expected 'GGUF'."
                  }
              }

              nativeHandle = getGGUFContextNativeHandleFromBuffer(buffer)
              assert(nativeHandle != 0L) { "Failed to load GGUF from buffer" }
          }


    fun getContextSize(): Long? {
        assert(nativeHandle != 0L) { "Use GGUFReader.load() to initialize the reader" }
        val contextSize = getContextSize(nativeHandle)
        return if (contextSize == -1L) {
            null
        } else {
            contextSize
        }
    }

    fun getChatTemplate(): String? {
        assert(nativeHandle != 0L) { "Use GGUFReader.load() to initialize the reader" }
        val chatTemplate = getChatTemplate(nativeHandle)
        return chatTemplate.ifEmpty { null }
    }

    /** Returns the native handle (pointer to gguf_context created on the native side) */
    private external fun getGGUFContextNativeHandle(modelPath: String): Long

    private external fun getGGUFContextNativeHandleFromFd(fd:Int): Long

    private external fun getGGUFContextNativeHandleFromBuffer(buffer: java.nio.ByteBuffer): Long

    /** Read the context size (in no. of tokens) from the GGUF file, given the native handle */
    private external fun getContextSize(nativeHandle: Long): Long

    /** Read the chat template from the GGUF file, given the native handle */
    private external fun getChatTemplate(nativeHandle: Long): String
}
