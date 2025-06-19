So this was a fun project I made using Java. I was inspired randomly by severe thunderstorms sweeping across the area of Ann Arbor, Michigan (where I live) on Jun 18, 2025. I have quite a bit of experience with terminals, DOS, Linux, etc. So, I made a weather app using Java (and Java Swing for the UI). I love command prompts and the insane amount of customization Linux offers and stuff like that. Most weather apps use a graphical user interface (GUI), but I wanted to make one with a command prompt for fun (and to hone my Java skills). With a little help from Microsoft Copilot, this is my final version. It's called TornadoAlert95 (the 95 for 1995, like Windows 95. Plus it has a 90s kind of vibe.) I hid a few Easter eggs in it too. I made the logo with Piskel.

To run:
Compile with a Java compiler (i.e from JDK) or use an IDE (like IntelliJ Idea, Eclipse, Apache NetBeans, etc) to run the TornadoAlert95 Java code.
Type "help" for a list of commands to run (I hid a few Easter eggs in there.)
  More detail:
  1. In an IDE (for this tutorial use IntelliJ IDEA), make a new project and call it TornadoAlert95.
  2. Name the new Java file inside the src folder TornadoAlert95.java.
  3. Paste the source code in the file.
  4. Create a package inside the src folder with the TornadoAlert95.java file inside. Name it com.example.TornadoAlert95.
  5. Compile and run. -> If there is errors about JSON, download this file from SourceForge: https://sourceforge.net/projects/json-java.mirror/files/latest/download. Then, make a folder named lib under the parent folder of everything. Add your JSON file to it. Go to File > Project Structure. Click on the Modules tab on the left. Click on the Dependencies tab. Hit the + button, click JARs or Directories, and add your JAR file to it. Make sure on the right it says Scope: Compile. If it doesn't, change it. Click Apply, then OK. Now there should be no more errors when you compile and run.
Welcome to TornadoAlert95!

Notes:
- Developed using IntelliJ IDEA.
- Took about a day to complete.
- Uses OpenWeatherMap's API service for real-time weather data.
- I used Linux as my developing OS.
- Made with 🧡
