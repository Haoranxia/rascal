package org.rascalmpl.library.experiments.tutor3;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.asciidoctor.Asciidoctor.Factory.create;
import static org.asciidoctor.AttributesBuilder.attributes;
import static org.asciidoctor.OptionsBuilder.options;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.Placement;
import org.asciidoctor.SafeMode;
import org.rascalmpl.library.experiments.Compiler.Commands.CommandOptions;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.NoSuchRascalFunction;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.ideservices.BasicIDEServices;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.values.ValueFactoryFactory;

import io.usethesource.vallang.IList;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;

/**
 * A course is organized as a collection of Concepts organized as a tree (maintained as an Onthology)
 * 
 * Each course MyCourse is represented by a top-level concept MyCourse.
 * 
 * Each concept C is represented in the file system as
 * - a directory C
 * - either a file C.concept (containing AsciiDoc markup)
 * - or a file C.remote (containing Rascal source code with @doc{} tags)
 * 
 * CourseCompiler compiles all courses to HTML in the following steps:
 * - each .concept file is translated to an AsciiDoc .adoc file.
 * - all generated AsciiDoc files are transformed to a single HTML file index.html per course
 * - the contributions to the Lucene index are computed and stored per course
 */
public class CourseCompiler {

    static void writeFile(String path, String content) throws IOException {
	  FileWriter fout = new FileWriter(path);
	  fout.write(content);
	  fout.close();
	}

    static void runAsciiDocter(Path srcPath, String courseName, Path destPath, PrintWriter err) throws IOException {
        runAsciiDoctor(create(), srcPath, courseName, destPath, err);
    }
    
	static void runAsciiDoctor(Asciidoctor asciidoctor, Path srcPath, String courseName, Path destPath, PrintWriter err) throws IOException {
	  Path courseDestDir = destPath.resolve(courseName);

	  Attributes attributes = attributes()
	      .tableOfContents(true)
	      .tableOfContents(Placement.LEFT)
	      .attribute("toc-title", courseName)
	      .attribute("numbered", true)
	      .attribute("verbose", true)
	      .attribute("linkcss", "true")
	      .attribute("stylesheet", "../css/style.css")
	      .get();

	  Options options = options()
	      .safe(SafeMode.UNSAFE)
	      .attributes(attributes)
	      .docType("book")
	      .destinationDir(new File(courseDestDir.toString()))
	      .baseDir(new File(courseDestDir.toString()))
	      .toFile(courseIndexFile(courseDestDir))
	      .get();

	  asciidoctor.convertFile(new File(courseADocFile(courseName, courseDestDir).toString()),  options);
	}

    public static File courseIndexFile(Path courseDestDir) {
        return new File(courseDestDir + "/" + "index.html");
    }
	
	static void runAsciiDoctorCommand(String classpath, Path srcPath, String courseName, Path destPath, PrintWriter err) throws IOException {
	    Path courseDestDir = destPath.resolve(courseName);

	    String cmd = 
	        "java -cp " + classpath
	        + " org.asciidoctor.cli.AsciidoctorInvoker"
	        + " -n"                                             // numbered sections
	        + " -v"                                             // verbose
	        + " -a toc-title=" + courseName                     // table of contents
	        + " -a toc=left"                                    // at left side
	        //+ " -a toclevels=2"
	        + " -a linkcss"                                     // link the style sheet
	        + " -a stylesheet=" + "../css/style.css"               // use our own style sheet
	        + " -d book"                                        // book style
	        + " -D " + courseDestDir                            // destination directory
	        + " -B " + courseDestDir                            // base directory
	        + " " + courseADocFile(courseName, courseDestDir) // the adoc source file
	        + " -o " + courseIndexFile(courseDestDir)       // the html output file
	        ;
	    Process p = Runtime.getRuntime().exec(cmd);
	    
	    try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
	        String line = null;

	        while ((line = input.readLine()) != null)
	        {
	            System.err.println(line);
	            err.println(line);
	        }

	        try {
	            int exitVal = p.waitFor();
	            if (exitVal != 0){
	                err.println("asciidoctor exits with error code " + exitVal);
	            }
	        } catch (InterruptedException e) {
	            e.printStackTrace(err);
	        }
	    }
	}

    public static Path courseADocFile(String courseName, Path courseDestDir) {
        return courseDestDir.resolve(courseName + ".adoc");
    }
	
	public static void compileCourse(Path srcPath, String courseName, Path destPath, Path libSrcPath, PathConfig pcfg, TutorCommandExecutor executor) throws IOException, NoSuchRascalFunction, URISyntaxException {
	    compileCourse(create(), srcPath, courseName, destPath, libSrcPath, pcfg, executor);
	}
	
    public static void compileCourse(Asciidoctor dr, Path srcPath, String courseName, Path destPath, Path libSrcPath, PathConfig pcfg, TutorCommandExecutor executor) throws IOException, NoSuchRascalFunction, URISyntaxException {
        copyStandardFilesPerCourse(srcPath, courseName, destPath);

        Onthology o = new Onthology(srcPath, courseName, destPath, libSrcPath, pcfg, executor);

        o.buildCourseMap();
        o.buildConcepts();

        runAsciiDoctor(dr, srcPath, courseName, destPath, executor.err);
	}
    
    public static void compileCourseCommand(String classpath, Path srcPath, String courseName, Path destPath, Path libSrcPath, PathConfig pcfg, TutorCommandExecutor executor) throws IOException, NoSuchRascalFunction, URISyntaxException {
        assert executor != null;
        copyStandardFilesPerCourse(srcPath, courseName, destPath);

        Onthology o = new Onthology(srcPath, courseName, destPath, libSrcPath, pcfg, executor);

        o.buildCourseMap();
        o.buildConcepts();

        runAsciiDoctorCommand(classpath, srcPath, courseName, destPath, executor.err);
    }
	
	private static void copyStandardFilesPerCourse(Path srcPath, String courseName, Path destPath) throws IOException {
		ArrayList<String> files  = new ArrayList<>();
		
		files.add("docinfo.html");
		Path coursePath = destPath.resolve(courseName);
		if(!Files.exists(coursePath)){
			Files.createDirectories(coursePath);
		}
		for(String file : files){
			Path src = srcPath.resolve(file);
			Path dest = coursePath.resolve(file);
			Path parent = dest.getParent();
			if(!Files.exists(parent)){
				Files.createDirectories(parent);
			}
			Files.copy(src, dest, REPLACE_EXISTING);
		}
	}
	
	/**
	 * @return true iff no files were copied because they are already in the destination path
	 */
	public static boolean copyStandardFiles(Path srcPath, Path destPath) throws IOException {
		
		System.err.println("Copying standard files");
		System.err.println("srcPath: " + srcPath + ", destPath: " + destPath);
		
		ArrayList<String> files  = new ArrayList<>();
		files.add("tutor-prelude.js");
		files.add("favicon.ico");
		files.add("css/style.css");
		files.add("docinfo.html");
		files.add("css/font-awesome.min.css");
		files.add("fonts/fontawesome-webfont.eot");
		files.add("fonts/fontawesome-webfont.svg");
		files.add("fonts/fontawesome-webfont.ttf");
		files.add("fonts/fontawesome-webfont.woff");
		files.add("fonts/fontawesome-webfont.woff2");
		
		files.add("images/rascal-tutor-small.png");
		files.add("images/good.png");
		files.add("images/bad.png");
		for(int i = 1; i <= 15; i++){
			files.add("images/" + i + ".png");
		}
		files.add("tutor-overview.include");
		
		for(String file : files){
			Path src = srcPath.resolve(file);
			Path dest = destPath.resolve(file);
			
			if (Files.exists(dest)) {
			    // break early, it already exists
			    return true;
			}
			
			Path parent = dest.getParent();
			if(!Files.exists(parent)){
				Files.createDirectories(parent);
			}
			//System.out.println("cp " + src + " " + dest);
			Files.copy(src, dest, REPLACE_EXISTING);
		}
		
		return false;
	}
	
	private static class RemoveAdocs extends SimpleFileVisitor<Path> {

		@Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException{
			String fileName = file.getFileName().toString();

			if(fileName.endsWith(".adoc")){
				Files.delete(file);
			}
			return FileVisitResult.CONTINUE;
		}
	}
	
	/**
	 * CourseCompiler: compile and deploy courses.
	 * 
	 * @param args array with command options and courses to be compiled
	 * @throws IOException
	 * @throws NoSuchRascalFunction
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws IOException, NoSuchRascalFunction, URISyntaxException {
		 IValueFactory vf = ValueFactoryFactory.getValueFactory();
		 long startTime = System.nanoTime();
		 CommandOptions cmdOpts = new CommandOptions("course-compiler");
         
         cmdOpts
         .locsOption("course")		
         .locsDefault(cmdOpts.getDefaultCourses().isEmpty() ? vf.list(cmdOpts.getDefaultCourses()) : cmdOpts.getDefaultCourses())
         .respectNoDefaults()
         .help("Add (absolute!) course location, use multiple --course arguments for multiple locations")
         
         .locsOption("src")		
         .locsDefault(cmdOpts.getDefaultStdlocs().isEmpty() ? vf.list(cmdOpts.getDefaultStdlocs()) : cmdOpts.getDefaultStdlocs())
         .respectNoDefaults()
         .help("Add (absolute!) source location, use multiple --src arguments for multiple locations")

         .locsOption("lib")		
         .locsDefault((co) -> vf.list(co.getCommandLocOption("bin")))
         .respectNoDefaults()
         .help("Add new lib location, use multiple --lib arguments for multiple locations")

         .locOption("bin") 		
         .respectNoDefaults()
         .help("Directory for Rascal binaries")
         
         .boolOption("skipCourses")
         .boolDefault(false)
         .help("Skip the compilation of courses")
         
         .boolOption("buildCourses")
         .boolDefault(true)
         .help("Skip the compilation of courses")
         
         .locOption("boot")         
         .help("Rascal boot directory")
         
         .boolOption("all")
         .help("Compile available courses")

         .boolOption("help") 		
         .help("Print help message for this command")

         .boolOption("verbose")
         .help("Make the course compiler verbose")

         .modules("Course modules to be compiled", 0)

         .handleArgs(args);
		
		PathConfig pcfg = 
				new PathConfig(cmdOpts.getCommandLocsOption("src"),
							   cmdOpts.getCommandLocsOption("lib"),
					           cmdOpts.getCommandLocOption("bin"),
					           cmdOpts.getCommandLocOption("boot"),
					           cmdOpts.getCommandLocsOption("course"));   
		
		Path coursesSrcPath = Paths.get(((ISourceLocation)pcfg.getCourses().get(0)).getURI());
		Path libSrcPath = Paths.get(((ISourceLocation)pcfg.getSrcs().get(0)).getURI());
		
		Path destPath = Paths.get(((ISourceLocation)pcfg.getBin()).getURI()).resolve("courses");
		
		if (
		    copyStandardFiles(coursesSrcPath, destPath)
		 ) {
//		    System.err.println("Bailing out because target files are already present...");
//		    return;
		}
		
		StringWriter sw = new StringWriter();
		PrintWriter err = new PrintWriter(sw);
		TutorCommandExecutor executor = new TutorCommandExecutor(pcfg, err, new BasicIDEServices(err));
		
		if (cmdOpts.getCommandBoolOption("skipCourses")) {
		    assert !cmdOpts.getCommandBoolOption("buildCourses");
		    System.err.println("Skipping compilation of courses.");
		    System.exit(0);
		}
		
		if(cmdOpts.getCommandBoolOption("all")){
			IList givenCourses = cmdOpts.getModules();
			if(!givenCourses.isEmpty()){
				System.err.println("--all conflicts with " + givenCourses);
			}
			for(String courseName : pcfg.listCourseEntries()){
				compileCourse(coursesSrcPath, courseName, destPath, libSrcPath, pcfg, executor);
			}
		} else {
			for(IValue iCourseName : cmdOpts.getModules()){
			    String courseName = ((IString) iCourseName).getValue();
			    ISourceLocation courseLoc = pcfg.getCourseLoc(courseName);
			    coursesSrcPath = Paths.get(courseLoc.getURI()).getParent();
				compileCourse(coursesSrcPath, ((IString)iCourseName).getValue(), destPath, libSrcPath, pcfg, executor);
			}
		}
		
		err.flush();
		writeFile(destPath + "/course-compilation-errors.txt", sw.toString());
		
		System.err.println("Removing intermediate files");
		
		FileVisitor<Path> fileProcessor = new RemoveAdocs();
		try {
			Files.walkFileTree(destPath, fileProcessor);
		} catch (IOException e) {
		    // TODO: handle file issue (one file failed) with proper error handling mechanism.
		    System.err.println(e.getMessage());
		}

		long duration = System.nanoTime() - startTime;
		System.err.println(String.format("Course compilation done after %,d ms\n", TimeUnit.NANOSECONDS.toMillis(duration)));
	}
}
