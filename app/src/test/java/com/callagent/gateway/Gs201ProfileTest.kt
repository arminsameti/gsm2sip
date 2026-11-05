package com.callagent.gateway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Gs201ProfileTest {

    @Test
    fun detectsPixel7ProByPlatformAndCodename() {
        // Values as reported by a Pixel 7 Pro on Android 17.
        assertTrue(DeviceProfile.isGs201("gs201", "gs201", "cheetah", "cheetah", "cheetah"))
        // Platform alone (another gs201 phone), codename alone (odd ROM).
        assertTrue(DeviceProfile.isGs201("gs201", "", "panther", "panther", "panther"))
        assertTrue(DeviceProfile.isGs201("", "", "cheetah", "", ""))
    }

    @Test
    fun doesNotClaimOtherDevices() {
        assertFalse(DeviceProfile.isGs201("sm6150", "sm7150", "surya", "surya", "qcom"))
        assertFalse(DeviceProfile.isGs201("gs101", "gs101", "oriole", "oriole", "oriole"))
        assertFalse(DeviceProfile.isGs201("", "", "", "", ""))
    }

    @Test
    fun usesStandardRoutes() {
        val p = DeviceProfile.gs201()
        assertTrue(p.voiceDownlinkWorks)
        assertTrue(p.playbackToTelephonyTx)
        // The "incall playback" mixPort is stereo-only.
        assertTrue(p.playbackStereo)
        assertFalse(p.requireSpeakerMode)
        assertEquals("", p.incallMusicParam)
        assertTrue(p.halCallActiveVsids.isEmpty())
    }

    @Test
    fun keepsVoiceCallStreamUnmutedAtFullVolume() {
        val p = DeviceProfile.gs201()
        assertTrue(p.silenceLocalAudio)
        assertFalse(p.muteVoiceCallStream)
        assertEquals(15, DeviceProfile.voiceCallVolIndex(p.voiceCallVolPercent, 15))
        // Other profiles keep the old mute.
        assertTrue(DeviceProfile.sm6150().muteVoiceCallStream)
    }

    @Test
    fun voiceCallVolIndexNeverZero() {
        assertEquals(1, DeviceProfile.voiceCallVolIndex(0, 15))
        assertEquals(1, DeviceProfile.voiceCallVolIndex(1, 15))
        assertEquals(10, DeviceProfile.voiceCallVolIndex(70, 15))
    }

    @Test
    fun mixerWritesOnlyRunBehindFallbackFlag() {
        val p = DeviceProfile.gs201()
        val guard = "if [ -e ${DeviceProfile.GS201_MIXER_FALLBACK_FLAG} ]; then "
        for (cmd in listOf(p.mixerSetupCmd, p.mixerRestoreCmd,
                           p.mixerIncallMusicCmd, p.mixerCaptureCmd)) {
            assertTrue(cmd, cmd.startsWith(guard))
            assertTrue(cmd, cmd.endsWith("; fi"))
        }
        assertTrue(p.mixerSetupCmd.contains("'Incall Capture Stream0' DL"))
        assertTrue(p.mixerSetupCmd.contains("'Voice Call Mic Source' IN_CALL_MUSIC"))
        // The flag path must survive resolveCmd's 'tinymix' substitution.
        assertFalse(DeviceProfile.GS201_MIXER_FALLBACK_FLAG.contains("tinymix"))
    }
}
