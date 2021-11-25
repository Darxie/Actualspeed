package cz.feldis.actualspeed.ktx.audio

import com.sygic.sdk.audio.AudioManager
import com.sygic.sdk.audio.AudioManagerProvider
import com.sygic.sdk.audio.AudioTTSOutput
import cz.feldis.actualspeed.ktx.SdkManagerKtx

class AudioManagerKtx : SdkManagerKtx<AudioManager>(AudioManagerProvider::getInstance) {
    suspend fun playTTS(text: String) {
        val manager = manager()
        val output = AudioTTSOutput(text)
        manager.playOutput(output)
    }
}