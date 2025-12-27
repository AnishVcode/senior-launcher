from fastapi import FastAPI
from pydantic import BaseModel
import json
import time
from groq import Groq
from intent_matcher import IntentMatcher
import uvicorn
from functools import lru_cache
import asyncio
import os
from dotenv import load_dotenv
load_dotenv()

# Compact intent dataset (keeping only essentials)
INTENT_DATASET = {
    "OPEN_CAMERA": [
        "open camera", "launch camera", "start camera", "camera please",
        "i want to take a picture", "show camera", "camera mode"
    ],
    "TAKE_SELFIE": [
        "take a selfie", "take my photo", "selfie mode", "selfie please",
        "front camera", "picture of me"
    ],
    "RECORD_VIDEO": [
        "record video", "start recording", "make a video", "video mode",
        "record me", "shoot a video"
    ],
    "OPEN_GALLERY": [
        "open gallery", "show my photos", "view pictures", "photo gallery",
        "see my images", "browse photos","open pictures"
    ],
    "OPEN_APP": [
        "open whatsapp", "launch youtube", "start google maps", "open gmail",
        "launch facebook", "start instagram", "open chrome"
    ],
    "CALL_CONTACT": [
        "call john", "phone my daughter", "dial sarah", "call mom",
        "make a call", "ring sister"
    ],
    "SEND_MESSAGE": [
        "text maria", "send message", "whatsapp my son", "sms john",
        "message my daughter", "send text", "text message"
    ],
    "SET_ALARM": [
        "set alarm", "wake me at", "alarm for morning",
        "set wake up alarm"
    ],
    "SET_TIMER": [
        "timer for", "set timer", "countdown", "start timer"
    ],
    "FLASHLIGHT_ON": [
        "turn on flashlight", "torch on", "flashlight on", "light on","it is very dark in here","i can't see anything"
    ],
    "FLASHLIGHT_OFF": [
        "turn off flashlight", "torch off", "flashlight off", "light off"
    ],
    "OPEN_SETTINGS": [
        "open settings", "wifi settings", "bluetooth settings", "system settings"
    ],
    "READ_NOTIFICATIONS": [
        "read notifications", "check notifications", "what messages",
        "show notifications"
    ],
    "VOLUME_CONTROL": [
        "volume up", "increase volume", "volume down", "decrease volume",
        "mute", "unmute", "louder", "quieter"
    ],
    "CHECK_BATTERY": [
        "battery level", "check battery", "battery percentage", "how much charge","could you please tell me if my phone has enough battery left for the rest of the day?","Tell me if I need to charge my phone"
    ],
    "MEDICATION_LIST_TODAY": [
        "what medicines today", "medication schedule", "list my pills",
        "today's medicines", "show medicine list"
    ],
    "MEDICATION_LOG_TAKEN": [
        "i took aspirin", "mark medicine taken", "took my medicine",
        "medicine taken", "completed medication"
    ],
    "MEDICATION_LOG_SKIP": [
        "skip dose", "missed my medicine", "didn't take pill", "skipping dose"
    ],
    "MEDICATION_ADD": [
        "add new medicine", "new prescription", "add medication", "new pill",
        "remind me to take my medicine", "add a reminder for paracetamol",
        "schedule a new pill", "remind me to take paracetamol everyday" 
    ],
    "MEDICATION_QUERY": [
        "when is my next dose",
        "what time is my next pill",
        "when do i take my medicine",
        "check my medication schedule",
        "what meds do i have today",
        "when is my paracetamol due",
        "do i have any pills to take now",
        "what time is my medication"
    ],
    "MEDICATION_HISTORY": [
        "medication log", "medicine history", "medication record", "pill history"
    ],
    "MEDICATION_SIDE_EFFECTS": [
        "side effects", "adverse effects", "medicine warnings", "medication risks"
    ],
    "APPOINTMENT_CREATE": [
        "book appointment", "schedule checkup", "make appointment", "see doctor"
    ],
    "APPOINTMENT_LIST": [
        "next appointment", "upcoming appointments", "show appointments"
    ],
    "APPOINTMENT_CANCEL": [
        "cancel appointment", "remove appointment", "delete appointment"
    ],
    "SOS_TRIGGER": [
        "emergency", "help me", "urgent help", "call for help", "sos"
    ],
    "HEALTH_CHECKIN_START": [
        "start health check", "record vitals", "health check in", "take vitals"
    ],
    "HEALTH_SUMMARY": [
        "health report", "health summary", "show health data", "vitals report"
    ],
    "HEALTH_RECORD": [
        "log blood sugar", "record blood pressure", "save measurement", "log vital"
    ],
    "CAREGIVER_CONTACT": [
        "call caregiver", "message nurse", "contact family", "call nurse"
    ],
    "QUERY_TIME": [
        "what time", "current time", "tell me the time"
    ],
    "QUERY_DATE": [
        "what's today", "what day", "current date", "today's date"
    ],
    "FIND_PHONE": [
        "find my phone", "where's my phone", "ring my phone"
    ],
    "REPEAT_LAST": [
        "say that again", "repeat", "what did you say", "one more time"
    ],
    "READ_SCREEN": [
        "read screen", "what's on screen", "describe display"
    ],
    "BRIGHTNESS_CONTROL": [
        "increase brightness", "dim screen", "adjust brightness"
    ],
    "DO_NOT_DISTURB": [
        "do not disturb", "silent mode", "mute notifications"
    ],
    "SCREEN_LOCK": [
        "lock phone", "lock screen", "turn off screen"
    ],
    "WEB_SEARCH": [
        "search for", "look up", "google", "find information"
    ],
    "QA_WEATHER": [
        "weather", "will it rain", "temperature"
    ],
    "SMALL_TALK_GREET": [
        "hello", "hi", "good morning", "good evening"
    ],
    "SMALL_TALK_THANKS": [
        "thank you", "thanks", "appreciate it"
    ],
    "SMALL_TALK_HELP": [
        "what can you do", "help me", "show capabilities"
    ],
    "BOOK_RIDE": [
        "book a ride", "call a cab", "get me a taxi", "order an uber",
        "book a ride to the doctor", "call an ola", "get a rapido",
        "i need to go home", "ride to pharmacy", "book a lyft"
    ]
}

print(f"🚀 Intents: {len(INTENT_DATASET)}")
print(f"📊 Examples: {sum(len(v) for v in INTENT_DATASET.values())}")


GROQ_API_KEY = os.getenv("GROQ_API_KEY")
client = Groq(api_key=GROQ_API_KEY)

app = FastAPI()

# Initialize matcher once at startup
print("🔧 Initializing Intent Matcher...")
matcher = IntentMatcher(INTENT_DATASET)
print("✅ Ready!")

class RefineRequest(BaseModel):
    text: str

# Streamlined system prompt (shorter = faster LLM)
# Updated System Prompt with strict constraints for Medication
SYSTEM_PROMPT_TEMPLATE = """You classify voice commands for SeniorOS.

USER: "{text}"

CANDIDATES:
{candidates}

RULES:
- "remind me to take" + med -> MEDICATION_ADD (extract medicationName, dosage, frequency)
- "add medicine/pill" -> MEDICATION_ADD
- "book/call/get" + ride/taxi/cab/uber/ola -> BOOK_RIDE (extract destination)
- "go to" + place -> BOOK_RIDE (extract destination)
- "send/text/message" + name -> SEND_MESSAGE (extract contactName)
- "call/phone/dial" + name -> CALL_CONTACT (extract contactName)
- "set alarm/wake me" + time -> SET_ALARM (extract time as "HH:MM" 24-hour format)
- "timer" + duration -> SET_TIMER (extract duration as number in minutes)
- Use top candidate if score > 0.4

JSON OUTPUT:
{{"intent":"X","reply":"Short confirmation","entities":{{"contactName":null,"appName":null,"medicationName":null,"dosage":null,"frequency":null,"time":null,"duration":null,"destination":null}}}}

EXAMPLES:
"wake me at 7 am" -> {{"intent":"SET_ALARM","reply":"Alarm set for 7:00 a.m.","entities":{{"time":"07:00"}}}}
"book a ride to the doctor" -> {{"intent":"BOOK_RIDE","reply":"Looking for a ride","entities":{{"destination":"doctor"}}}}
"get me an uber to home" -> {{"intent":"BOOK_RIDE","reply":"Opening Uber","entities":{{"destination":"home"}}}}
"""

@app.get("/")
async def root():
    return {"status": "online", "message": "LLM Intent Server"}

@app.post("/refine")
async def refine_intent(request: RefineRequest):
    """Optimized intent classification"""
    print(f"\n📥 '{request.text}'")
    start = time.time()

    # 1. Vector Search (target: <10ms)
    t0 = time.time()
    candidates = matcher.get_top_candidates(request.text, top_k=3)  # Reduced to 3
    search_ms = (time.time() - t0) * 1000

    # Format candidates (compact)
    candidates_str = "\n".join([
        f"{c['intent']} ({c['score']:.2f})"
        for c in candidates
    ])

    # 2. LLM (target: <800ms)
    prompt = SYSTEM_PROMPT_TEMPLATE.format(
        text=request.text,
        candidates=candidates_str
    )
    
    t1 = time.time()
    try:
        completion = client.chat.completions.create(
            messages=[
                {"role": "system", "content": prompt},
                {"role": "user", "content": request.text}
            ],
            model="llama-3.1-8b-instant",
            temperature=0.0,
            max_tokens=150,  # Limit response length
            response_format={"type": "json_object"}
        )
        result = json.loads(completion.choices[0].message.content)
        print(f"✓ {result.get('intent')}")
    except Exception as e:
        print(f"✗ {e}")
        result = {
            "intent": "SMALL_TALK_HELP",
            "reply": "Could you rephrase that?",
            "entities": {}
        }
    
    llm_ms = (time.time() - t1) * 1000
    total_ms = (time.time() - start) * 1000

    print(f"⏱️ Search:{search_ms:5.0f}ms | LLM:{llm_ms:5.0f}ms | Total:{total_ms:5.0f}ms\n")

    return {
        "intent": result.get("intent", "SMALL_TALK_HELP"),
        "reply": result.get("reply", "I'm here to help."),
        "entities": result.get("entities", {})
    }

@app.get("/health")
async def health():
    return {
        "status": "healthy",
        "intents": len(INTENT_DATASET),
        "examples": sum(len(v) for v in INTENT_DATASET.values())
    }

if __name__ == "__main__":
    print("\n" + "="*50)
    print("🚀 SENIOROSLAUNCHER LLM SERVER")
    print("="*50)
    print(f"📍 http://0.0.0.0:8000")
    print(f"🎯 POST /refine")
    print("="*50 + "\n")
    
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=8000,
        log_level="warning"
    )