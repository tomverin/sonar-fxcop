package org.sonar.plugins.fxcop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class CSharpProjectInfo {
	private static final String LIBRARY = "Library";
	private static final String NETCOREAPP = "netcoreapp";
	private static final Logger LOG = Loggers.get(FxCopSensor.class);
	static final Pattern patternType = Pattern.compile("<OutputType>([\\w]+)</OutputType>");
	static final Pattern patternName = Pattern.compile("<AssemblyName>([\\w\\-\\ \\.]+)</AssemblyName>");
	static final Pattern patternPath = Pattern.compile("<OutputPath>([\\w\\-\\ \\.\\\\\\$\\(\\)]+)</OutputPath>");
	static final Pattern patternTargetFramework = Pattern.compile("<TargetFramework>([\\w\\-\\ \\.\\\\]+)</TargetFramework>");
	static final Pattern patternTargetFrameworks = Pattern.compile("<TargetFrameworks>([\\w\\-\\ \\.\\\\;]+)</TargetFrameworks>");
	static final Pattern patternVariable = Pattern.compile("$\\(([\\w]+)\\)");
	
	private String project = null;
	private String name = null;
	private String type = null;
	private String targetFramework;
	private List<String> paths = new ArrayList<>();
	
	CSharpProjectInfo(String project) throws IOException{
		this.project = project;
		scanProjectFile();
		performOnNetCore();
		checkAllRequiredValuesFound();
	}
	
	public boolean isDotNetCore(){
		if (targetFramework==null)return false;
		return targetFramework.toLowerCase().contains(NETCOREAPP);
	}

	private void performOnNetCore() {
		//Net core project files contain only non default settings, so set defaults if not set
		if (targetFramework!=null && targetFramework.startsWith(NETCOREAPP)){
			if (paths.isEmpty()) {
				paths.add(convertPath("bin\\Debug\\netcoreapp2.0"));
				paths.add(convertPath("bin\\Release\\netcoreapp2.0"));
				paths.add(convertPath("bin\\Debug\\netcoreapp2.1"));
				paths.add(convertPath("bin\\Release\\netcoreapp2.1"));
				LOG.debug("Set Outputpath to default");
			}
			if (type == null){
				type = LIBRARY;
				LOG.debug("Set OutputType to default (Library)");
			}
			if (name == null) {
				File projectFile = new File(project);
				name = projectFile.getName().replace(".csproj", "");
				LOG.debug("Set AssemblyName to default ("+name+")");
			}
		}
		
	}

	private void checkAllRequiredValuesFound() {
		if (paths == null || paths.isEmpty()) {
	    	LOG.warn("No output path found for '"+project+"'.");
	    	throw new IllegalArgumentException("No output path found for '"+project+"'.");
	    }
	    if (type == null) {
	    	LOG.warn("No output type found for '"+project+"'.");
	    	throw new IllegalArgumentException("No output type found for '"+project+"'.");
	    }
	    if (name == null || name.isEmpty()) {
	    	LOG.warn("No output name found for '"+project+"'.");
	    	throw new IllegalArgumentException("No output name found for '"+project+"'.");
	    }
		
	}

	private void scanProjectFile() throws IOException {
		final BufferedReader reader = new BufferedReader(new FileReader(new File(project)));
		try {
	       while(reader.ready()) {
	    	   String currentLine = reader.readLine();
	    	   Matcher m = patternType.matcher(currentLine);
	           if (m.find()) {
	        	   type = (m.group(1));
	        	   LOG.debug("Found OutputType ("+type+")");
	           }
	           m = patternName.matcher(currentLine);
	           if (m.find()) {
	        	   name = (m.group(1));
	        	   LOG.debug("Found AssemblyName ("+name+")");
	           }
	           m = patternPath.matcher(currentLine);
	           if (m.find()) {
	        	   String path = convertPath(m.group(1));	        	   
	        	   addPath(path);
	        	   LOG.debug("Found OutputPath ("+path+")");
	           }
	           m = patternTargetFramework.matcher(currentLine);
	           if (m.find()) {
	        	   targetFramework = m.group(1);	
	        	   LOG.debug("Found TargetFramework ("+targetFramework+")");
	           }
	           m = patternTargetFrameworks.matcher(currentLine);
	           if (m.find()) {
	        	   setTargetFrameworkFromList(m.group(1));	
	        	   LOG.debug("Found TargetFramework ("+targetFramework+")");
	           }
	       }
		} finally {
	       reader.close();
		}
	}
	
	private void setTargetFrameworkFromList(String listOfTargetFrameworks) {
		String[] list = listOfTargetFrameworks.split(";");
		targetFramework = null;
		
		for (String currentTargetFramework : list) {
			if (!currentTargetFramework.startsWith("netcore") &&
					!currentTargetFramework.startsWith("netstandard")){
				targetFramework = currentTargetFramework;
				addPathForFramework(targetFramework);
			}
		}
		if (targetFramework!= null){
			setTypeForMultitargetProject();
			setNameForMultitargetProject();
			return;
		}
		
		LOG.warn("Found multitarget: '" + listOfTargetFrameworks + "' but none of those is supported.");
		targetFramework = NETCOREAPP;//not supported
	}

	private void setNameForMultitargetProject() {
		if (name == null) {
			File projectFile = new File(project);
			name = projectFile.getName().replace(".csproj", "");
			LOG.debug("Set AssemblyName to default ("+name+")");
		}
		
	}

	private void setTypeForMultitargetProject() {
		if (type == null){
			type = LIBRARY;
			LOG.debug("Set OutputType to default (Library)");
		}
		
		
	}

	private void addPathForFramework(String targetFramework) {
		paths.add(convertPath("bin\\Debug\\"+targetFramework));
		paths.add(convertPath("bin\\Release\\"+targetFramework));
		
	}

	private void addPath(String path) {
		boolean added = addIfNoVariables(path);
		if (added) return;
		
		String adaptedPath = path;
		
		
		
		added = solveBuildConfigurationAndAdd(adaptedPath);
		if (added) return;
		
		handleUnreplaceableVariables(adaptedPath);
	}

	private boolean solveBuildConfigurationAndAdd(String path) {
		String adaptedPath = path.replace("$(BuildConfiguration)", "Debug");
		boolean added = addIfNoVariables(adaptedPath);
		if (added) {
			addIfNoVariables(path.replace("$(BuildConfiguration)", "Release"));
			return true;
		}
		handleUnreplaceableVariables(adaptedPath);
		return false;
	}

	private void handleUnreplaceableVariables(String adaptedPath) {
		Matcher m = patternType.matcher(adaptedPath);
        if (m.find()) {
     	   String variableName = (m.group(1));
     	  LOG.error("Variable '"+variableName+"' found in output path, is not supported. Please define scan properties in command line or SonarQube.Analysis.xml.");
			throw new IllegalStateException("Variable '"+variableName+"' found in output path, is not supported. Please define scan properties in command line or SonarQube.Analysis.xml.");
        }
        LOG.error("Path '"+adaptedPath+"' found in output path, is not supported. Please define scan properties in command line or SonarQube.Analysis.xml.");
		throw new IllegalStateException("Path '"+adaptedPath+"' found in output path, is not supported. Please define scan properties in command line or SonarQube.Analysis.xml.");
	}

	private boolean addIfNoVariables(String path) {
		if (!path.contains("$(")) {
			paths.add(path);
			return true;
		}
		return false;
	}

	private String convertPath(String path){
		if (File.pathSeparator.equals("\\")) {
 		   return path;
 	   }
		return path.replace('\\', '/');
	}

	public String getDllPathFromExistingBinary() {
		File projectFile = new File(project);
		String binFileName = getBinFileName(type, name);
	    Path result = null;
	    String parentDir = projectFile.getParent();
	    if (parentDir==null) parentDir = ".";
	    StringBuilder sbPath = new StringBuilder();
	    
	    for (String path : paths) {
			try {
				result = Paths.get(parentDir, path, binFileName).toRealPath();
				break;
			} catch (IOException ex){
				if (sbPath.length()>0) sbPath.append(", ");
				sbPath.append(Paths.get(parentDir, path).toString());
				LOG.info(ex.getMessage());
				result = null;
			}
		}
	    if (result == null) {
			LOG.error(binFileName + " was not found in any output directory ("+sbPath+"), please build project before scan.");
			throw new IllegalStateException(binFileName + " was not found in any output directory ("+sbPath+"), please build project before scan.");
	    }
		return result.toString();
	}
	
	private String getBinFileName(String type, String name) {
		if (type.equalsIgnoreCase(LIBRARY)){
			return name + ".dll";
		}
		return name + ".exe";
	}
}
