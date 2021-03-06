package org.sonar.plugins.fxcop;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FxCopProjectGeneratorTest {
	private static String TEST_SLN = "src/test/resources/FxCopConfigGeneratorTests/TestApp1.sln";
	private static String TEST_SLN_NO_EXITING_PROJECTS = "src/test/resources/FxCopConfigGeneratorTests/TestApp2.sln";
	private static String TEST_SLN_EMPTY = "src/test/resources/FxCopConfigGeneratorTests/TestApp3.sln";
	private static String TEST_CORE_SLN = "src/test/resources/FxCopConfigGeneratorTests/TestAppCore.sln";
	private static String TEST_NO_PATH_SLN = "src/test/resources/FxCopConfigGeneratorTests/TestAppNoPath.sln";
	private static String TEST_NO_TYPE_SLN = "src/test/resources/FxCopConfigGeneratorTests/TestAppNoType.sln";
	private static String TEST_NO_NAME_SLN = "src/test/resources/FxCopConfigGeneratorTests/TestAppNoName.sln";
	private static String TEST_CSPROJ_DLL = "src/test/resources/FxCopConfigGeneratorTests/TestLib1.csproj";
	private static String TEST_CSPROJ_Replaceable_DLL = "src/test/resources/FxCopConfigGeneratorTests/TestLib1Replaceable.csproj";
	private static String TEST_CSPROJ_None_Replaceable_DLL = "src/test/resources/FxCopConfigGeneratorTests/TestLib1NoneReplaceable2.csproj";
	private static String TEST_CSPROJ_EXE = "src/test/resources/FxCopConfigGeneratorTests/TestApp1.csproj";
	private static String TEST_CORE_CSPROJ_DLL = "src/test/resources/FxCopConfigGeneratorTests/TestLibCore.csproj";
	private static String TEST_MULTI_NO_SUPPORT_CSPROJ_DLL = "src/test/resources/FxCopConfigGeneratorTests/TestLibMultiNoSupport.csproj";
	private static String TEST_MULTI_SUPPORT_CSPROJ_DLL = "src/test/resources/FxCopConfigGeneratorTests/TestLibMultiSupport.csproj";
	static String TEST_DLL_RELEASE = "src/test/resources/bin/Release/TestLib1.dll";
	static String TEST_EXE_RELEASE = "src/test/resources/bin/Release/TestApp1.exe";
	static String TEST_DLL_DEBUG = "src/test/resources/bin/Debug/TestLib1.dll";
	private static String TEST_CORE_DLL_DEBUG = "src/test/resources/FxCopConfigGeneratorTests/bin/Debug/netcoreapp2.0/TestLibCore.dll";
	private static String TEST_MULTI_DLL_DEBUG = "src/test/resources/FxCopConfigGeneratorTests/bin/Debug/net452/TestLibMultiSupport.dll";
	static String TEST_EXE_DEBUG = "src/test/resources/bin/Debug/TestApp1.exe";
	
	
	@Rule
	  public ExpectedException thrown = ExpectedException.none();
	
	@Before
	public void setUp() throws Exception {
		File binFolder = new File("src/test/resources/bin/Release");
		binFolder.mkdirs();
		binFolder = new File("src/test/resources/bin/Debug");
		binFolder.mkdirs();
		binFolder = new File("src/test/resources/bin/Debug/net452");
		binFolder.mkdirs();
		binFolder = new File("src/test/resources/bin/Release/net452");
		binFolder.mkdirs();
	}

	@After
	public void tearDown() throws Exception {
		File binFolder = new File("src/test/resources/bin/Release");
		deleteFolder(binFolder);
		binFolder = new File("src/test/resources/bin/Debug");
		deleteFolder(binFolder);
	}
	
	public static void deleteFolder(File folder) {
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}

	  @Test
	  public void testGenerate() throws IOException {
		  FxCopProjectGenerator gen = new FxCopProjectGenerator();
		  
		  File binFile = new File(TEST_EXE_RELEASE);
		  assertThat(binFile.createNewFile()).isTrue();
		  assertThat(Files.exists(Paths.get(binFile.getAbsolutePath()))).isTrue();
		  binFile = new File(TEST_DLL_RELEASE);
		  assertThat(binFile.createNewFile()).isTrue();
		  assertThat(Files.exists(Paths.get(binFile.getAbsolutePath()))).isTrue();
		  
		  String resultFileName = gen.generate(TEST_SLN);
		  
		  assertThat(resultFileName).isNotEmpty();
		  
		  
	  }
	  
	  @Test(expected = IllegalArgumentException.class)
	  public void testGenerateCore() throws IOException {
		  FxCopProjectGenerator gen = new FxCopProjectGenerator();
		  
		  File binFile = new File(TEST_CORE_DLL_DEBUG);
		  new File( binFile.getParent()).mkdirs();
		  binFile.createNewFile();
		  assertThat(Files.exists(Paths.get(binFile.getAbsolutePath()))).isTrue();
		  
		  String resultFileName = gen.generate(TEST_CORE_SLN);
		  //No .net Core support in the moment
		  //assertThat(resultFileName).isNotEmpty();
		  
		  
	  }
	  
	  @Test(expected = IllegalArgumentException.class)
	  public void testSlnNoPath() throws IOException {
		  new CSharpProjectInfo(TEST_NO_PATH_SLN.replace(".sln", ".csproj").replace("TestApp", "TestLib"));
		  
	  }
	  
	  @Test(expected = IllegalArgumentException.class)
	  public void testSlnNoType() throws IOException {
		  new CSharpProjectInfo(TEST_NO_TYPE_SLN.replace(".sln", ".csproj").replace("TestApp", "TestLib"));
		  
	  }
	  
	  @Test(expected = IllegalArgumentException.class)
	  public void testSlnNoName() throws IOException {
		  new CSharpProjectInfo(TEST_NO_NAME_SLN.replace(".sln", ".csproj").replace("TestApp", "TestLib"));
		  
	  }
	  
	  @Test(expected = IllegalStateException.class)
	  public void testGenerateNonExistingSlnFile() throws IOException {
		  FxCopProjectGenerator gen = new FxCopProjectGenerator();
		  gen.generate("NotExisting.sln");
	  }
	  
	  @Test(expected = IllegalStateException.class)
	  public void testGenerateNonExistingCsprojFile() throws IOException {
		  FxCopProjectGenerator gen = new FxCopProjectGenerator();
		  gen.generate(TEST_SLN_NO_EXITING_PROJECTS);
	  }
	  
	  @Test(expected = IllegalArgumentException.class)
	  public void testGenerateEmptySlnFile() throws IOException {
		  FxCopProjectGenerator gen = new FxCopProjectGenerator();
		  gen.generate(TEST_SLN_EMPTY);
	  }
	  
	  @Test
	  public void testSlnScan() throws IOException {
		  FxCopProjectGenerator gen = new FxCopProjectGenerator();
		  File slnFile = new File(TEST_SLN);
		  slnFile = new File(slnFile.getAbsolutePath());
		  
		  String[] csprojs = gen.getCsprojForSolution(slnFile);
		  
		  assertThat(csprojs.length).isEqualTo(2);
		  assertThat(csprojs[0]).endsWith("TestApp1.csproj");
		  assertThat(csprojs[1]).endsWith("TestLib1.csproj");
		  
		  File exeConfFile = new File(TEST_CSPROJ_EXE);
		  File dllConfFile = new File(TEST_CSPROJ_DLL);
		  
		  assertThat(new File(csprojs[0]).getAbsolutePath()).isEqualTo(exeConfFile.getAbsolutePath());
		  assertThat(new File(csprojs[1]).getAbsolutePath()).isEqualTo(dllConfFile.getAbsolutePath());
	  }
	  
	  @Test
	  public void testCsprojDllReleaseScan() throws IOException {
		  FxCopProjectGenerator gen = new FxCopProjectGenerator();
		  
		  File binFile = new File(TEST_DLL_RELEASE);
		  binFile.createNewFile();
		  assertThat(Files.exists(Paths.get(binFile.getAbsolutePath()))).isTrue();
		  
		  String file = gen.getDllPathFromCsProj(TEST_CSPROJ_DLL);
		  
		  assertThat(file).isNotNull();
		  assertThat(file).isNotEmpty();
		  assertThat(file).endsWith("TestLib1.dll");
		  assertThat(file).doesNotContain("/../");
		  assertThat(file).doesNotContain("\\..\\");
	  }
	  
	  
	  
	  @Test
	  public void testCsprojExeReleseScan() throws IOException {
		  FxCopProjectGenerator gen = new FxCopProjectGenerator();
		  
		  File binFile = new File(TEST_EXE_RELEASE);
		  binFile.createNewFile();
		  assertThat(Files.exists(Paths.get(binFile.getAbsolutePath()))).isTrue();
		  
		  String file = gen.getDllPathFromCsProj(TEST_CSPROJ_EXE);
		  
		  assertThat(file).isNotNull();
		  assertThat(file).isNotEmpty();
		  assertThat(file).endsWith("TestApp1.exe");
		  assertThat(file).doesNotContain("/../");
		  assertThat(file).doesNotContain("\\..\\");
	  }
	  
	  @Test
	  public void testCoreCsprojDllDebugScan() throws IOException {
		  FxCopProjectGenerator gen = new FxCopProjectGenerator();
		  
		  File binFile = new File(TEST_CORE_DLL_DEBUG);
		  new File( binFile.getParent()).mkdirs();
		  binFile.createNewFile();
		  assertThat(Files.exists(Paths.get(binFile.getAbsolutePath()))).isTrue();
		  
		  String file = gen.getDllPathFromCsProj(TEST_CORE_CSPROJ_DLL);
		  
		  assertThat(file).isNull();
		  /* No .Net Core support in the moment
		  assertThat(file).isNotEmpty();
		  assertThat(file).endsWith("TestLibCore.dll");
		  assertThat(file).doesNotContain("/../");
		  assertThat(file).doesNotContain("\\..\\");*/
	  }
	  
	  @Test
	  public void testMultiNoSupportCsprojDllDebugScan() throws IOException {
		  FxCopProjectGenerator gen = new FxCopProjectGenerator();
		  
		  File binFile = new File(TEST_CORE_DLL_DEBUG);
		  new File( binFile.getParent()).mkdirs();
		  binFile.createNewFile();
		  assertThat(Files.exists(Paths.get(binFile.getAbsolutePath()))).isTrue();
		  
		  String file = gen.getDllPathFromCsProj(TEST_MULTI_NO_SUPPORT_CSPROJ_DLL);
		  
		  assertThat(file).isNull();
		  /* No .Net Core support in the moment
		  assertThat(file).isNotEmpty();
		  assertThat(file).endsWith("TestLibCore.dll");
		  assertThat(file).doesNotContain("/../");
		  assertThat(file).doesNotContain("\\..\\");*/
	  }
	  
	  @Test
	  public void testMultiSupportCsprojDllDebugScan() throws IOException {
		  FxCopProjectGenerator gen = new FxCopProjectGenerator();
		  
		  File binFile = new File(TEST_MULTI_DLL_DEBUG);
		  new File( binFile.getParent()).mkdirs();
		  binFile.createNewFile();
		  assertThat(Files.exists(Paths.get(binFile.getAbsolutePath()))).isTrue();
		  
		  String file = gen.getDllPathFromCsProj(TEST_MULTI_SUPPORT_CSPROJ_DLL);
		  
		  assertThat(file).isNotNull();
		  assertThat(file).isNotEmpty();
		  assertThat(file).endsWith("TestLibMultiSupport.dll");
		  assertThat(file).doesNotContain("/../");
		  assertThat(file).doesNotContain("\\..\\");
	  }
	  
	  @Test
	  public void testCsprojDllDebugScan() throws IOException {
		  FxCopProjectGenerator gen = new FxCopProjectGenerator();
		  
		  File binFile = new File(TEST_DLL_DEBUG);
		  assertThat(binFile.createNewFile()).isTrue();
		  assertThat(Files.exists(Paths.get(binFile.getAbsolutePath()))).isTrue();
		  
		  String file = gen.getDllPathFromCsProj(TEST_CSPROJ_DLL);
		  
		  assertThat(file).isNotNull();
		  assertThat(file).isNotEmpty();
		  assertThat(file).endsWith("TestLib1.dll");
		  assertThat(file).doesNotContain("/../");
		  assertThat(file).doesNotContain("\\..\\");
	  }
	  
	  @Test
	  public void testCsprojDllReplaceableDebugScan() throws IOException {
		  FxCopProjectGenerator gen = new FxCopProjectGenerator();
		  
		  File binFile = new File(TEST_DLL_DEBUG);
		  assertThat(binFile.createNewFile()).isTrue();
		  assertThat(Files.exists(Paths.get(binFile.getAbsolutePath()))).isTrue();
		  
		  String file = gen.getDllPathFromCsProj(TEST_CSPROJ_Replaceable_DLL);
		  
		  assertThat(file).isNotNull();
		  assertThat(file).isNotEmpty();
		  assertThat(file).endsWith("TestLib1.dll");
		  assertThat(file).doesNotContain("/../");
		  assertThat(file).doesNotContain("\\..\\");
	  }
	  
	  @Test(expected = IllegalStateException.class)
	  public void testCsprojDllNoneReplaceableDebugScan() throws IOException {
		  FxCopProjectGenerator gen = new FxCopProjectGenerator();
		  
		  File binFile = new File(TEST_DLL_DEBUG);
		  assertThat(binFile.createNewFile()).isTrue();
		  assertThat(Files.exists(Paths.get(binFile.getAbsolutePath()))).isTrue();
		  
		  String file = gen.getDllPathFromCsProj(TEST_CSPROJ_None_Replaceable_DLL);		  	
	  }
	  
	  @Test
	  public void testCsprojExeDebugScan() throws IOException {
		  FxCopProjectGenerator gen = new FxCopProjectGenerator();
		  
		  File binFile = new File(TEST_EXE_DEBUG);
		  assertThat(binFile.createNewFile()).isTrue();
		  assertThat(Files.exists(Paths.get(binFile.getAbsolutePath()))).isTrue();
		  
		  String file = gen.getDllPathFromCsProj(TEST_CSPROJ_EXE);
		  
		  assertThat(file).isNotNull();
		  assertThat(file).isNotEmpty();
		  assertThat(file).endsWith("TestApp1.exe");
		  assertThat(file).doesNotContain("/../");
		  assertThat(file).doesNotContain("\\..\\");
	  }
	  
	  @Test(expected = IllegalStateException.class)
	  public void testCsprojNoBinary() throws IOException {
		  FxCopProjectGenerator gen = new FxCopProjectGenerator();
		  
		  gen.getDllPathFromCsProj(TEST_CSPROJ_EXE);		  
	  }
}
