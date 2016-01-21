
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class UmlParserfinal {
	// no static here. use only when its necessary.
	public static String s = "@startuml\n";
	URLClassLoader cl;

	// method to delete files with .class extension
	// naming
	// nice little method. This is how a method should be. 
	// you can still refactor to say deleteClassFile a separatemethod and call that in this method in the for loop.
	// easy to test each action that way
	public void DeleteClassFiles(String arg) {
		File dir = new File(arg);
		File[] content = dir.listFiles();
		for (File del : content) {
			if (del.isFile() && del.getName().endsWith(".class"))
				del.delete();
		}
	}

	// converting a .java file to .class file
	// naming
	// you say convert to Class,. Why is not returning the class object.
	// dont use static for this.
	// method should be like public Class convertToClass(String classname)
	public void ConvertToClass(String arg) {
		try {
			File dir = new File(arg);
			File[] content = dir.listFiles();
			// Convert File to a URL
			URL url = dir.toURL(); // file:/c:/myclasses/
			URL[] urls = new URL[] { url };
			// Create a new class loader with the directory
			cl = new URLClassLoader(urls);
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
			StandardJavaFileManager fileManager = compiler
					.getStandardFileManager(diagnostics, null, null);
			Iterable<? extends JavaFileObject> compilationUnits = fileManager
					.getJavaFileObjects(dir.listFiles());
			JavaCompiler.CompilationTask task = compiler.getTask(null,
					fileManager, diagnostics, null, null, compilationUnits);
			boolean success = task.call();
			fileManager.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// add .class names to List
	// you say add to the list. why does it not return the list? 
	public void AddClassNamestoList(File currentFile) {
		ArrayList<String> className = new ArrayList<String>();
		if (currentFile.isFile() && currentFile.getName().endsWith(".class")) {
			String nameOfClass;
			nameOfClass = currentFile.getName();
			nameOfClass = nameOfClass
					.substring(0, nameOfClass.lastIndexOf('.'));
			className.add(nameOfClass);
		}
		for (String temp : className) {
			GetInterfaces(temp);
		}
		GetAssociations(className);
	}

	// get interfaces and superclasses
	private void GetInterfaces(String temp) {
		try {
			// you should not load the class everytime from string.
			// time consuming operation. load it once and pass around the Class object.
			Class<?> cls = cl.loadClass(temp);
			String clname = cls.getSimpleName();// getclassname
			Class[] interfaces = cls.getInterfaces();
			for (Class i : interfaces) {
				s = s + i + "\n";
				if (cls.getName().contains("implements"))
					s = s + clname + "\n";
			}
			Class<?>[] impls = cls.getInterfaces();
			Class ex = cls.getSuperclass();
			String claname = cls.getSimpleName();
			if (impls != null && ex != null) {
				s = s + "class" + " " + claname + "\n";
				for (Class i : interfaces)
					s = s + i + " " + "<|.." + " " + clname + "\n";
				if (!ex.getName().contains("java."))
					s = s + ex + " " + "<|--" + " " + claname + "\n";
			} else if (impls == null || ex == null) {
				// string builder. read on stringbuilder vs string appending.
				s = s + "class" + " " + claname + "\n";
			} else if (impls != null) {
				for (Class<?> i : interfaces)
					s = s + i + " " + "<|.." + " " + claname + "\n";
			}
			getFields(temp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// get Fields
	// whats temp ?
	public void getFields(String temp) {
		try {
			// you should not load the class everytime from string.
			// time consuming operation. load it once and pass around the Class object.
			Class<?> cls = cl.loadClass(temp);
			String clname = cls.getSimpleName();
			Field[] fields = cls.getDeclaredFields();// getfields
			for (Field field : fields) {
				String mo = null;
				String mod = Modifier.toString(field.getModifiers());
				if (mod.equals("public"))
					mo = "+";
				if (mod.equals("private"))
					mo = "-";
				if (mod.equals("protected"))
					mo = "#";
				String type = field.getType().getSimpleName();
				String tname = field.getName();
				Class match = field.getType();
				String t = field.getType().toString();
				if (!field.getType().getSimpleName().contains("Collection")
						&& !type.equalsIgnoreCase(tname.toUpperCase()))
					s = s + clname + " : " + mo + " " + " " + tname + ": "
							+ type + "\n";
				if (field.getType().getSimpleName().contains("Collection"))
					s = s + clname + " -- " + "\"many\"" + " "
							+ tname.toUpperCase() + "\n";
				if (field.getType().getSimpleName()
						.contains(tname.toUpperCase()))
					s = s + clname + " -- " + "\"1\" " + " "
							+ tname.toUpperCase() + "\n";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// naming conventions.
	// a method should not cross beyond 25 lines. if it does, time to refactor into small methods. - break this one.
	// its hard to read this method and it has lots of nested loops. - Dont do too much nested loops.
	public void GetAssociations(ArrayList<String> classNames) {
		// whats tempname. use proper name. see how you name below
		// classNames = listOfClassNames
		// tempName = className
		ArrayList<String> tempName = classNames;
		try {
			for (String temp : tempName) {
				// you should not load the class everytime from string.
				// time consuming operation. load it once and pass around the Class object.
				Class<?> cls = cl.loadClass(temp);
				// naming
				String clname = cls.getSimpleName();
				Method[] allMethod1 = cls.getDeclaredMethods();
				for (Method method : allMethod1) {
					for (String temp1 : tempName) {
						// you should not load the class everytime from string.
						// time consuming operation. load it once and pass around the Class object.
						Class<?> cls1 = cl.loadClass(temp1);// loadclass
						String clsname = cls1.getSimpleName();
						Method[] all = cls1.getDeclaredMethods();
						for (Method meth : all) {
							if (method.getName().equalsIgnoreCase(
									meth.getName())) {
								if (cls.isInterface()
										&& !s.contains(clname + "<.." + clsname)
										&& !clsname.equalsIgnoreCase(clname))
									// dont use static string to these manipulations.
									// pass in string or return strings.
									// why not use stringbuilder instead of strings? - read and modify
									s = s + clname + "<.." + " " + clsname
											+ "\n";
							}
							if (cls.isInterface()
									&& meth.getName().contains("main"))
								s = s + clname + "<.." + " " + clsname + "\n";
						}
					}
					Class[] params = method.getParameterTypes();
					for (int k = 0; k < params.length; k++) {
						Class a = params[k];
						String parameterString = params[k].getName();
						if (!parameterString.contains("java.")
								&& !s.contains(parameterString + "<.." + clname
										+ ":uses"))
							s = s + parameterString + "<.." + clname + ":uses"
									+ "\n";
					}
					if (Modifier.isPublic(method.getModifiers())) {
						String metmod = null;
						String me = Modifier.toString(method.getModifiers());
						if (me.equals("public"))
							metmod = "+";
						if (me.equals("private"))
							metmod = "-";
						Class metret = method.getReturnType();
						String metname = method.getName();
						if (isGetter(method)) {
							String mename;
							mename = method.getName();
							s = s + clname + " : " + metmod + " " + mename
									+ " " + "(" + ")" + ":" + metret + "\n";
						}
						if (isSetter(method)) {
							String mname;
							mname = method.getName();
							s = s + clname + " : " + metmod + " " + mname + " "
									+ "(" + ")" + ":" + metret + "\n";
						}
						s = s + clname + " : " + metmod + " " + metname + " "
								+ "(" + ")" + ":" + metret + "\n";
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// why static
	public static boolean isGetter(Method method) {
		if (!method.getName().startsWith("get"))
			return false;
		if (method.getParameterTypes().length != 0)
			return false;
		if (void.class.equals(method.getReturnType()))
			return false;
		return true;
	}

	// why static
	public static boolean isSetter(Method method) {
		if (!method.getName().startsWith("set"))
			return false;
		if (method.getParameterTypes().length != 1)
			return false;
		return true;
	}

	public static void main(String[] args) {
		try {
			// dont give unrecoqnizable variable names. its not useful. 
			// google on camecasing for java and variable names.
			UmlParserfinal obj = new UmlParserfinal();
			// Why is there a hard coded path ? remove hard coded paths.
			String argument = "C:\\input\\foo4\\";
			// method names shouldnt start with capital letters.
			// its a naming convention. google the java appropriate naming convention
			obj.DeleteClassFiles(argument);// Delete all the existing class files
			// naming convention						
			obj.ConvertToClass(argument);// converting a .java file to .class file
			File dir = new File(argument);// list all files in the directory
			File[] allFiles = dir.listFiles();
			for (File currentFile : allFiles) {
				// naming convention
				obj.AddClassNamestoList(currentFile);// add the names of classe to the List

			}
			// why static string. Dont use static unless you need to
			// tell me why you used static string first.
			s = s + "@enduml\n";
			UmlGenerator p = new UmlGenerator();
			// name of the method should be an action. class name should be the Noun. UML generator is correct for the class.
			// whats uml creator means ? is it generateUml()? you should know what a method does just by reading its name
			p.umlCreator(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
