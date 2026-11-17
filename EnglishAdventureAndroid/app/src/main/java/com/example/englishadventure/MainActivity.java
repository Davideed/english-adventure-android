package com.example.englishadventure;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends Activity {
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private SharedPreferences prefs;
    private int cardIndex = 0, quizIndex = 0, quizScore = 0, stars = 0;
    private boolean answered = false, quizFinished = false;
    private String selected = null;
    private LinearLayout root, content;
    private int listenIndex = 0, listenScore = 0, listenRound = 0;
    private int memoryTurn = 0, memoryFirst = -1, memorySecond = -1, memoryMatches = 0;
    private boolean[] memoryDone = new boolean[10];
    private int feedIndex = 0, feedScore = 0;
    private int balloonRound = 0, balloonScore = 0, balloonTarget = 0;
    private boolean balloonAnswered = false, balloonLastCorrect = false;
    private String feedback = "";

    private final String[] en = {"CAT", "DOG", "BIRD", "FISH", "RABBIT"};
    private final String[] cn = {"猫", "小狗", "小鸟", "鱼", "兔子"};
    private final String[] emoji = {"🐱", "🐶", "🐦", "🐠", "🐰"};
    private final String[] ipa = {"/kæt/", "/dɔːɡ/", "/bɜːrd/", "/fɪʃ/", "/ˈræbɪt/"};
    private final String[] sentences = {"I see a cat!", "I love dogs!", "A little bird.", "I see a fish!", "Look at the rabbit!"};
    private final String[] meanings = {"我看到一只猫！", "我喜欢狗！", "一只小鸟。", "我看到一条鱼！", "看那只兔子！"};
    private final String[][] choices = {
        {"DOG", "CAT", "FISH"}, {"BIRD", "RABBIT", "DOG"},
        {"BIRD", "CAT", "FISH"}, {"RABBIT", "FISH", "DOG"},
        {"CAT", "BIRD", "RABBIT"}
    };
    private final String[] foodEn = {"FISH", "BONE", "SEEDS", "FISH FOOD", "CARROT"};
    private final String[] foodEmoji = {"🐟", "🦴", "🌻", "🫧", "🥕"};
    private final String[] foodCn = {"小鱼", "骨头", "种子", "鱼食", "胡萝卜"};
    private final int[] foodForAnimal = {0,1,2,3,4};
    private final int[] memoryDeck = {0,1,2,3,4,0,1,2,3,4};

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("adventure_progress", MODE_PRIVATE);
        stars = prefs.getInt("stars", 0);
        initTts();
        showHome();
    }

    private void initTts() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                           result != TextToSpeech.LANG_NOT_SUPPORTED;
                if (!ttsReady) Toast.makeText(this, "请在手机设置中安装英语语音数据。", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void speak(String value) {
        if (ttsReady && tts != null) {
            tts.setSpeechRate(0.78f);
            tts.setPitch(1.08f);
            tts.speak(value, TextToSpeech.QUEUE_FLUSH, null, "english_adventure");
        } else Toast.makeText(this, "英语语音尚未准备好，请检查手机的文字转语音设置。", Toast.LENGTH_LONG).show();
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setGravity(Gravity.CENTER); t.setPadding(dp(8),dp(8),dp(8),dp(8)); return t;
    }
    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(16),dp(12),dp(16),dp(18)); return l;
    }
    private void base(String title, String subtitle) {
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(255,249,237)); setContentView(root);
        LinearLayout header = new LinearLayout(this); header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(18),dp(16),dp(18),dp(16)); header.setBackground(bg(Color.rgb(37,75,131),0));
        TextView h = text(title,23,Color.WHITE,true); h.setGravity(Gravity.START);
        TextView sub = text(subtitle,13,Color.rgb(220,235,255),false); sub.setGravity(Gravity.START);
        header.addView(h); header.addView(sub); root.addView(header);
        ScrollView scroll = new ScrollView(this); content = column(); scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    }
    private void space(int h) { View v=new View(this); content.addView(v,new LinearLayout.LayoutParams(1,dp(h))); }
    private Button button(String label, int color, Runnable action) {
        Button b=new Button(this); b.setText(label); b.setTextSize(16); b.setAllCaps(false);
        b.setTextColor(Color.WHITE); b.setBackground(bg(color,18)); b.setPadding(dp(12),dp(10),dp(12),dp(10));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,dp(5),0,dp(5));
        b.setLayoutParams(p); b.setOnClickListener(v->action.run()); return b;
    }
    private void panel(String title, String desc, int color) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12),dp(10),dp(12),dp(10)); box.setBackground(bg(color,18));
        box.addView(text(title,19,Color.rgb(37,75,131),true));
        box.addView(text(desc,13,Color.DKGRAY,false));
        content.addView(box);
    }
    private void addStar(int n) {
        stars += n; prefs.edit().putInt("stars",stars).apply();
    }

    private void showHome() {
        base("🌈 英语冒险岛","ENGLISH ADVENTURE · 动物王国");
        TextView hero=text("🏝️ 🐱 🐶 🐰",38,Color.BLACK,false);
        hero.setBackground(bg(Color.rgb(226,241,255),22));
        content.addView(hero,new LinearLayout.LayoutParams(-1,dp(112)));
        content.addView(text("欢迎来到动物王国！",23,Color.rgb(37,75,131),true));
        content.addView(text("和小动物交朋友，一起玩游戏学英语！",15,Color.DKGRAY,false));
        panel("⭐ 我的星星："+stars,"每答对一题，收集一颗星星！",Color.rgb(255,239,196));
        space(10);
        content.addView(button("📖 认识动物单词",Color.rgb(55,133,197),()->{cardIndex=0;showLearn();}));
        content.addView(button("🎮 游戏乐园（4种小游戏）",Color.rgb(42,153,105),()->showGames()));
        content.addView(button("🔊 听听英语怎么说",Color.rgb(125,91,175),()->speak("Hello! Welcome to English Adventure!")));
        space(8);
        content.addView(text("今天玩15分钟：学一学 · 听一听 · 闯一关",13,Color.GRAY,false));
    }

    private void showLearn() {
        base("📖 动物单词卡","认识动物朋友 · "+(cardIndex+1)+" / 5");
        TextView animal=text(emoji[cardIndex],76,Color.BLACK,false);
        animal.setBackground(bg(Color.rgb(232,245,255),24));
        content.addView(animal,new LinearLayout.LayoutParams(-1,dp(135)));
        content.addView(text(en[cardIndex],34,Color.rgb(37,91,153),true));
        content.addView(text(cn[cardIndex],24,Color.rgb(52,72,95),true));
        content.addView(text(ipa[cardIndex],16,Color.GRAY,false));
        content.addView(button("🔊 听单词发音",Color.rgb(55,133,197),()->speak(en[cardIndex])));
        content.addView(text(sentences[cardIndex],21,Color.rgb(37,91,153),true));
        content.addView(text(meanings[cardIndex],14,Color.GRAY,false));
        content.addView(button("🔊 听完整句子",Color.rgb(125,91,175),()->speak(sentences[cardIndex])));
        space(8);
        if(cardIndex>0) content.addView(button("上一个",Color.GRAY,()->{cardIndex--;showLearn();}));
        content.addView(button(cardIndex==4?"学完啦！去玩游戏 🎮":"下一个动物 →",Color.rgb(42,153,105),()->{
            if(cardIndex==4)showGames();else{cardIndex++;showLearn();}
        }));
        content.addView(button("返回首页",Color.GRAY,()->showHome()));
    }

    private void showGames() {
        base("🎪 游戏乐园","选择一个小游戏，开始冒险！");
        panel("🌟 游戏规则","玩游戏、听英语、答对题目，就能收集星星！",Color.rgb(255,239,196));
        space(8);
        content.addView(button("🎧 听声音找动物",Color.rgb(75,145,190),()->startListen()));
        content.addView(text("听一听英文单词，找出正确的小动物。",14,Color.GRAY,false));
        content.addView(button("🧠 记忆翻翻乐",Color.rgb(135,103,184),()->startMemory()));
        content.addView(text("翻开卡片，找到相同的动物配对。",14,Color.GRAY,false));
        content.addView(button("🎈 单词气球大冒险",Color.rgb(225,105,125),()->startBalloons()));
        content.addView(text("听单词、找气球，戳破正确的动物气球！",14,Color.GRAY,false));
        content.addView(button("🍎 给动物送礼物",Color.rgb(222,139,62),()->startFeed()));
        content.addView(text("帮小动物找到它最喜欢的食物！",14,Color.GRAY,false));
        content.addView(button("🔤 单词复习闯关",Color.rgb(42,153,105),()->startQuiz()));
        content.addView(button("返回首页",Color.GRAY,()->showHome()));
    }

    private void startQuiz(){quizIndex=0;quizScore=0;answered=false;selected=null;quizFinished=false;showQuiz();}
    private void showQuiz() {
        base("🔤 单词复习闯关","第 "+(quizIndex+1)+" / 5 题");
        if(quizFinished){content.addView(text("🏆 闯关完成！",28,Color.rgb(37,75,131),true));
            content.addView(text(quizScore+" / 5 分",30,Color.rgb(190,135,35),true));
            content.addView(button("再玩一次",Color.rgb(55,133,197),()->startQuiz()));
            content.addView(button("游戏乐园",Color.rgb(42,153,105),()->showGames()));return;}
        content.addView(text("⭐ 当前得分："+quizScore,17,Color.rgb(180,125,25),true));
        TextView animal=text(emoji[quizIndex],70,Color.BLACK,false);animal.setBackground(bg(Color.rgb(232,245,255),24));
        content.addView(animal,new LinearLayout.LayoutParams(-1,dp(130)));
        content.addView(text("这只小动物用英语怎么说？",20,Color.rgb(37,75,131),true));
        for(String option:choices[quizIndex]){
            int c=selected!=null&&selected.equals(option)?(option.equals(en[quizIndex])?Color.rgb(42,153,105):Color.rgb(190,75,65)):Color.rgb(70,130,180);
            content.addView(button(option,c,()->answerQuiz(option)));
        }
        if(answered){
            boolean ok=selected.equals(en[quizIndex]);
            content.addView(text(ok?"🎉 太棒了！+1⭐":"💪 没关系！答案是 "+en[quizIndex],18,ok?Color.rgb(35,130,70):Color.rgb(170,85,45),true));
            content.addView(button(quizIndex==4?"查看成绩":"下一题 →",Color.rgb(42,153,105),()->{
                if(quizIndex==4){quizFinished=true;showQuiz();}else{quizIndex++;answered=false;selected=null;showQuiz();}
            }));
        }
        content.addView(button("游戏乐园",Color.GRAY,()->showGames()));
    }
    private void answerQuiz(String option){if(answered)return;selected=option;answered=true;if(option.equals(en[quizIndex])){quizScore++;addStar(1);}showQuiz();}

    // Mini-game 1: listen, then identify the animal.
    private void startListen(){listenIndex=0;listenScore=0;listenRound=0;showListen();}
    private void showListen(){
        base("🎧 听声音找动物","听英文，选出对应的小动物");
        if(listenRound>=5){
            content.addView(text("🎉 听力小达人！",27,Color.rgb(37,75,131),true));
            content.addView(text(listenScore+" / 5 答对",25,Color.rgb(190,135,35),true));
            content.addView(button("再玩一次",Color.rgb(55,133,197),()->startListen()));
            content.addView(button("游戏乐园",Color.GRAY,()->showGames()));return;
        }
        int target=(listenRound*3+1)%5; listenIndex=target;
        content.addView(text("第 "+(listenRound+1)+" 题 / 5",17,Color.GRAY,true));
        content.addView(text("🔊",62,Color.BLACK,false));
        content.addView(text("听一听！它的英文名字是什么？",19,Color.rgb(37,75,131),true));
        content.addView(button("再听一次英语单词",Color.rgb(125,91,175),()->speak(en[listenIndex])));
        ArrayList<Integer> opts=new ArrayList<>();opts.add(target);
        for(int i=0;opts.size()<3;i++){int v=(target+i+2)%5;if(!opts.contains(v))opts.add(v);}
        Collections.shuffle(opts);
        for(int v:opts) content.addView(button(emoji[v]+"  "+cn[v],Color.rgb(70,130,180),()->{
            if(v==listenIndex){listenScore++;addStar(1);Toast.makeText(this,"答对啦！⭐",Toast.LENGTH_SHORT).show();}
            else Toast.makeText(this,"再记住这个单词："+en[listenIndex],Toast.LENGTH_SHORT).show();
            listenRound++;showListen();
        }));
        content.addView(button("返回游戏乐园",Color.GRAY,()->showGames()));
    }

    // Mini-game 2: memory matching. Tap two cards; matching animals disappear.
    private void startMemory(){
        memoryTurn=0;memoryFirst=-1;memorySecond=-1;memoryMatches=0;
        memoryDone=new boolean[10];for(int i=0;i<10;i++)memoryDeck[i]=i%5;
        // deterministic shuffle of pairs
        for(int i=9;i>0;i--){int j=(int)(Math.random()*(i+1));int tmp=memoryDeck[i];memoryDeck[i]=memoryDeck[j];memoryDeck[j]=tmp;}
        showMemory();
    }
    private void showMemory(){
        base("🧠 记忆翻翻乐","找到5组相同的小动物");
        content.addView(text("已配对："+memoryMatches+" / 5",18,Color.rgb(37,75,131),true));
        for(int row=0;row<2;row++){
            LinearLayout line=new LinearLayout(this);line.setOrientation(LinearLayout.HORIZONTAL);
            for(int col=0;col<5;col++){
                int idx=row*5+col;
                String label=memoryDone[idx]?"✓":(memoryFirst==idx||memorySecond==idx?emoji[memoryDeck[idx]]+" "+en[memoryDeck[idx]]:"❔");
                Button b=new Button(this);b.setText(label);b.setTextSize(13);b.setAllCaps(false);
                b.setTextColor(Color.WHITE);b.setBackground(bg(memoryDone[idx]?Color.rgb(42,153,105):Color.rgb(83,133,185),14));
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(76),1);lp.setMargins(dp(2),dp(4),dp(2),dp(4));line.addView(b,lp);
                final int card=idx;b.setOnClickListener(v->flipCard(card));
            }
            content.addView(line);
        }
        if(memoryMatches==5){
            content.addView(text("🏆 全部配对成功！",24,Color.rgb(42,153,105),true));
            content.addView(button("再玩一次",Color.rgb(55,133,197),()->startMemory()));
        }
        content.addView(button("游戏乐园",Color.GRAY,()->showGames()));
    }
    private void flipCard(int card){
        if(memoryDone[card]||card==memoryFirst||memorySecond!=-1)return;
        if(memoryFirst==-1){memoryFirst=card;showMemory();speak(en[memoryDeck[card]]);return;}
        memorySecond=card;
        if(memoryDeck[memoryFirst]==memoryDeck[memorySecond]){
            memoryDone[memoryFirst]=true;memoryDone[memorySecond]=true;memoryMatches++;addStar(1);
            memoryFirst=-1;memorySecond=-1;
            Toast.makeText(this,"配对成功！⭐",Toast.LENGTH_SHORT).show();
            showMemory();
        }else{
            showMemory();
            content.postDelayed(()->{memoryFirst=-1;memorySecond=-1;showMemory();},950);
        }
    }

    // Mini-game 3: feed the animals.
    private void startFeed(){feedIndex=0;feedScore=0;feedback="";showFeed();}
    private void showFeed(){
        base("🍎 给动物送礼物","帮助小动物找到喜欢的食物");
        if(feedIndex>=5){
            content.addView(text("🥳 动物们吃饱啦！",25,Color.rgb(37,75,131),true));
            content.addView(text(feedScore+" / 5 次成功",22,Color.rgb(190,135,35),true));
            content.addView(button("再玩一次",Color.rgb(55,133,197),()->startFeed()));
            content.addView(button("游戏乐园",Color.GRAY,()->showGames()));return;
        }
        content.addView(text("第 "+(feedIndex+1)+" / 5 只小动物",16,Color.GRAY,true));
        content.addView(text(emoji[feedIndex],76,Color.BLACK,false));
        content.addView(text("Hello! I'm a "+en[feedIndex]+".",20,Color.rgb(37,75,131),true));
        content.addView(button("🔊 听小动物说话",Color.rgb(125,91,175),()->speak("Hello! I'm a "+en[feedIndex]+". I am hungry!")));
        content.addView(text("我饿啦！请给我找食物：",17,Color.DKGRAY,false));
        for(int i=0;i<5;i++){
            final int food=i;
            content.addView(button(foodEmoji[i]+"  "+foodEn[i]+"（"+foodCn[i]+"）",Color.rgb(222,139,62),()->{
                if(food==foodForAnimal[feedIndex]){
                    feedScore++;addStar(1);Toast.makeText(this,"Yummy! 好吃！⭐",Toast.LENGTH_SHORT).show();
                    speak("Yummy! Thank you!");
                }else Toast.makeText(this,"再试试别的食物吧！",Toast.LENGTH_SHORT).show();
                if(food==foodForAnimal[feedIndex]){feedIndex++;showFeed();}
            }));
        }
        content.addView(button("游戏乐园",Color.GRAY,()->showGames()));
    }

    // Mini-game 4: listen and pop the matching animal balloon.
    private void startBalloons(){
        balloonRound=0;balloonScore=0;balloonAnswered=false;balloonLastCorrect=false;showBalloons();
    }
    private void showBalloons(){
        base("🎈 单词气球大冒险","听一听，戳破正确的气球！");
        if(balloonRound>=5){
            content.addView(text("🎉 气球冒险完成！",27,Color.rgb(37,75,131),true));
            content.addView(text(balloonScore+" / 5 答对",26,Color.rgb(190,135,35),true));
            content.addView(text("你收集到了 "+balloonScore+" 颗星星！",17,Color.DKGRAY,false));
            content.addView(button("再玩一次",Color.rgb(225,105,125),()->startBalloons()));
            content.addView(button("游戏乐园",Color.GRAY,()->showGames()));
            return;
        }
        balloonTarget=(balloonRound*2+3)%5;
        content.addView(text("第 "+(balloonRound+1)+" / 5 关",17,Color.GRAY,true));
        content.addView(text("🎧 听听这个单词：",19,Color.rgb(37,75,131),true));
        content.addView(button("🔊 播放英语单词",Color.rgb(125,91,175),()->speak(en[balloonTarget])));
        content.addView(text("点一点击中它的气球！",18,Color.DKGRAY,true));
        ArrayList<Integer> opts=new ArrayList<>();opts.add(balloonTarget);
        for(int i=0;opts.size()<3;i++){int v=(balloonTarget+i+1)%5;if(!opts.contains(v))opts.add(v);}
        Collections.shuffle(opts);
        int[] balloonColors={Color.rgb(238,110,125),Color.rgb(76,163,213),Color.rgb(246,178,67)};
        for(int i=0;i<opts.size();i++){
            final int animal=opts.get(i);
            Button b=button("🎈\n"+emoji[animal]+"\n"+cn[animal],balloonColors[i],()->{
                if(balloonAnswered)return;
                balloonAnswered=true;
                balloonLastCorrect=(animal==balloonTarget);
                if(balloonLastCorrect){
                    balloonScore++;addStar(1);
                    speak("Great job! "+en[balloonTarget]+"!");
                    Toast.makeText(this,"啪！答对啦！⭐",Toast.LENGTH_SHORT).show();
                }else{
                    speak("This is "+en[balloonTarget]+".");
                    Toast.makeText(this,"再听一次，答案是 "+en[balloonTarget],Toast.LENGTH_SHORT).show();
                }
                showBalloonsResult();
            });
            b.setTextSize(19);
            content.addView(b,new LinearLayout.LayoutParams(-1,dp(92)));
        }
        content.addView(button("返回游戏乐园",Color.GRAY,()->showGames()));
    }
    private void showBalloonsResult(){
        content.addView(text(balloonLastCorrect?"🌟 真棒！":"💪 再试一次！",20,
            balloonLastCorrect?Color.rgb(42,153,105):Color.rgb(180,110,45),true));
        content.addView(button("下一关 →",Color.rgb(42,153,105),()->{
            balloonRound++;balloonAnswered=false;showBalloons();
        }));
    }

    @Override protected void onDestroy(){if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
}
