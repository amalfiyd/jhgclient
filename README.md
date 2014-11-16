## Suggestions

Get a normal IDE. I'd suggest Intellij Idea Community ediction from http://www.jetbrains.com/idea/download/ .
Eclipse will also work for us (get it at https://www.eclipse.org/downloads/)

## What to do
Open *src/masdar/jh/JhBotExample.java* and have look at *makeMove* function.
Right now there is an example of how to write simple tit-fot-tat bot. You can figure out hte rest from it.
Set TOKEN variable to the token you have for your bot.

Note that current implementation will join the first game in the list so in order
to modify the logic of joining search for "GAME JOINING PART IS HERE!" comment.

## How to compile the program if you are not using any IDE
If you prefer hardcore notepad programing, to compile and run follow next steps.
Open the terminal or command prompt, whatever you have in your OS for that (cmd.exe in windows, xterm in linux, Terminal in MAC)
and cd to the *src* folder, for example
```
cd juniorhigh/src
```


then write
```
javac masdar/jh/*
```


This will compile the java files. To run them type
```
java masdar.jh.JhBotExample
```

