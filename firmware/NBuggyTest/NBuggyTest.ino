#include <WiFi.h>
#include <WebServer.h>
#include <Preferences.h>
#include "interface.h"

// BENCH TEST ONLY: deliberately no motor GPIO outputs.
WebServer server(80);
Preferences prefs;
uint32_t onSeconds=5, offSeconds=15, noFlowSeconds=3;
bool running=false, feeding=false, product=true, fault=false;
uint32_t phaseAt=0, lastProduct=0;
String apName;
const char* password="NBuggyTest26";

bool auth(){
  if(server.authenticate("admin",password)) return true;
  server.requestAuthentication(); return false;
}
void stopCycle(){running=false;feeding=false;}
bool number(const String &s,uint32_t &v){
  if(!s.length() || s.length()>5)return false;
  for(size_t i=0;i<s.length();i++)if(s[i]<'0'||s[i]>'9')return false;
  v=s.toInt();return true;
}
void status(){
  if(!auth())return;
  String state=fault?"NO FLOW":!running?"STOPPED":feeding?"FEEDING":"WAITING";
  uint32_t duration=feeding?onSeconds:offSeconds;
  uint32_t elapsed=(millis()-phaseAt)/1000;
  String body="{\"state\":\""+state+"\",\"running\":"+(running?String("true"):String("false"));
  body+=",\"feeding\":"+String(feeding?"true":"false")+",\"product\":"+String(product?"true":"false");
  body+=",\"fault\":"+String(fault?"true":"false")+",\"on\":"+String(onSeconds)+",\"off\":"+String(offSeconds);
  body+=",\"delay\":"+String(noFlowSeconds)+",\"remaining\":"+String(running&&elapsed<duration?duration-elapsed:0);
  body+=",\"ssid\":\""+apName+"\",\"bench\":true}";
  server.sendHeader("Cache-Control","no-store");server.send(200,"application/json",body);
}
void setup(){
  Serial.begin(115200);
  prefs.begin("nbuggy-test",false);
  onSeconds=prefs.getUInt("on",5);offSeconds=prefs.getUInt("off",15);noFlowSeconds=prefs.getUInt("delay",3);
  if(onSeconds<1||onSeconds>900||offSeconds<3*onSeconds||offSeconds>86400||noFlowSeconds<1||noFlowSeconds>onSeconds){onSeconds=5;offSeconds=15;noFlowSeconds=3;}
  apName="N-Buggy-Test-"+String((uint32_t)(ESP.getEfuseMac()&0xFFFFFF),HEX);
  WiFi.mode(WIFI_AP);WiFi.softAP(apName.c_str(),password);
  server.on("/",HTTP_GET,[]{if(auth())server.send_P(200,"text/html",PAGE);});
  server.on("/api/status",HTTP_GET,status);
  server.on("/api/settings",HTTP_POST,[]{
    if(!auth())return;
    uint32_t a,b,c;
    if(!number(server.arg("on"),a)||!number(server.arg("off"),b)||!number(server.arg("delay"),c)||a<1||a>900||b<3*a||b>86400||c<1||c>a){server.send(400,"text/plain","Use ON 1-900s, OFF >= 3 x ON (max 86400s), delay 1-ON seconds.");return;}
    if(running){server.send(409,"text/plain","Stop the test before changing settings.");return;}
    onSeconds=a;offSeconds=b;noFlowSeconds=c;
    prefs.putUInt("on",a);prefs.putUInt("off",b);prefs.putUInt("delay",c);
    server.send(200,"text/plain","Settings saved on ESP32");
  });
  server.on("/api/command",HTTP_POST,[]{
    if(!auth())return;
    String cmd=server.arg("cmd");uint32_t now=millis();
    if(cmd=="stop")stopCycle();
    else if(cmd=="start"){
      if(fault){server.send(409,"text/plain","Reset fault first");return;}
      if(!running){running=true;feeding=true;phaseAt=lastProduct=now;}
    }
    else if(cmd=="product_on")product=true;
    else if(cmd=="product_off")product=false;
    else if(cmd=="reset"){if(running){server.send(409,"text/plain","Stop first");return;}fault=false;}
    else {server.send(400,"text/plain","Unknown command");return;}
    server.send(200,"text/plain","Command accepted");
  });
  server.begin();Serial.println("BENCH ONLY - NO MOTOR OUTPUTS");Serial.println(apName);Serial.println(WiFi.softAPIP());
}
void loop(){
  server.handleClient();uint32_t now=millis();
  if(!running)return;
  if(feeding){
    if(product)lastProduct=now;
    // Fault evaluated before phase change so short feed cycles are checked.
    if(now-lastProduct>=noFlowSeconds*1000UL){fault=true;stopCycle();return;}
    if(now-phaseAt>=onSeconds*1000UL){feeding=false;phaseAt=now;}
  } else if(now-phaseAt>=offSeconds*1000UL){feeding=true;phaseAt=lastProduct=now;}
}
