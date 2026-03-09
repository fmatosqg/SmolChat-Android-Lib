package io.shubham0204.smolchat.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmolLMClientTest {

    @Test
    fun testAssetExists() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val assetManager = context.assets
        val inputStream = assetManager.open("test_model.gguf")
        assertNotNull(inputStream)
        inputStream.close()
    }
}
