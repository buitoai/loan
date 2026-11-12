"""Text-to-Speech service"""
import os
from TTS.api import TTS


class TTSService:
    """Service for text-to-speech generation"""
    
    def __init__(self):
        self.model_name = os.getenv("TTS_MODEL", "tts_models/en/ljspeech/tacotron2-DDC")
        self.tts = None
        
    def load_model(self):
        """Load TTS model"""
        if self.tts is None:
            print(f"Loading TTS model: {self.model_name}")
            self.tts = TTS(self.model_name)
            print("TTS model loaded")
    
    async def generate_speech(
        self,
        text: str,
        output_path: str
    ) -> str:
        """
        Generate speech from text
        """
        self.load_model()
        
        try:
            # Generate audio
            self.tts.tts_to_file(
                text=text,
                file_path=output_path
            )
            
            return output_path
            
        except Exception as e:
            print(f"Error generating speech: {e}")
            raise Exception(f"TTS generation failed: {str(e)}")
