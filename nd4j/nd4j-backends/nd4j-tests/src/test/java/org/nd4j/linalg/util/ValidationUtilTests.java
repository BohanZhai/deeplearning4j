package org.nd4j.linalg.util;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nd4j.linalg.BaseNd4jTest;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.factory.Nd4jBackend;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.validation.Nd4jCommonValidator;
import org.nd4j.validation.ValidationResult;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

public class ValidationUtilTests extends BaseNd4jTest {

    @Rule
    public TemporaryFolder testDir = new TemporaryFolder();

    public ValidationUtilTests(Nd4jBackend backend) {
        super(backend);
    }

    @Test
    public void testFileValidation() throws Exception {
        File f = testDir.newFolder();

        //Test not existent file:
        File fNonExistent = new File("doesntExist.bin");
        ValidationResult vr0 = Nd4jCommonValidator.isValidFile(fNonExistent);
        assertFalse(vr0.isValid());
        assertTrue(vr0.getIssues().get(0), vr0.getIssues().get(0).contains("exist"));
        System.out.println(vr0.toString());

        //Test empty file:
        File fEmpty = new File(f, "0.bin");
        fEmpty.createNewFile();
        ValidationResult vr1 = Nd4jCommonValidator.isValidFile(fEmpty);
        assertFalse(vr1.isValid());
        assertTrue(vr1.getIssues().get(0), vr1.getIssues().get(0).contains("empty"));
        System.out.println(vr1.toString());

        //Test directory
        File directory = new File(f, "dir");
        boolean created = directory.mkdir();
        assertTrue(created);
        ValidationResult vr2 = Nd4jCommonValidator.isValidFile(directory);
        assertFalse(vr2.isValid());
        assertTrue(vr2.getIssues().get(0), vr2.getIssues().get(0).contains("directory"));
        System.out.println(vr2.toString());

        //Test valid non-empty file - valid
        File f3 = new File(f, "1.txt");
        FileUtils.writeStringToFile(f3, "Test", StandardCharsets.UTF_8);
        ValidationResult vr3 = Nd4jCommonValidator.isValidFile(f3);
        assertTrue(vr3.isValid());
        System.out.println(vr3.toString());
    }

    @Test
    public void testZipValidation() throws Exception {
        File f = testDir.newFolder();

        //Test not existent file:
        File fNonExistent = new File("doesntExist.zip");
        ValidationResult vr0 = Nd4jCommonValidator.isValidZipFile(fNonExistent, false);
        assertFalse(vr0.isValid());
        assertTrue(vr0.getIssues().get(0), vr0.getIssues().get(0).contains("exist"));
        System.out.println(vr0.toString());

        //Test empty zip:
        File fEmpty = new ClassPathResource("validation/empty_zip.zip").getFile();
        assertTrue(fEmpty.exists());
        ValidationResult vr1 = Nd4jCommonValidator.isValidZipFile(fEmpty, false);
        assertFalse(vr1.isValid());
        assertTrue(vr1.getIssues().get(0), vr1.getIssues().get(0).contains("empty"));
        System.out.println(vr1.toString());

        //Test directory (not zip file)
        File directory = new File(f, "dir");
        boolean created = directory.mkdir();
        assertTrue(created);
        ValidationResult vr2 = Nd4jCommonValidator.isValidFile(directory);
        assertFalse(vr2.isValid());
        assertTrue(vr2.getIssues().get(0), vr2.getIssues().get(0).contains("directory"));
        System.out.println(vr2.toString());

        //Test non-empty zip - valid
        File f3 = new File(f, "1.zip");
        try(ZipOutputStream z = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(f3)))){
            ZipEntry ze = new ZipEntry("content.txt");
            z.putNextEntry(ze);
            z.write("Text content".getBytes());
        }
        ValidationResult vr3 = Nd4jCommonValidator.isValidZipFile(f3, false);
        assertTrue(vr3.isValid());
        System.out.println(vr3.toString());

        //Test non-empty zip - but missing required entries
        ValidationResult vr4 = Nd4jCommonValidator.isValidZipFile(f3, false, "content.txt", "someFile1.bin", "someFile2.bin");
        assertFalse(vr4.isValid());
        assertEquals(1, vr4.getIssues().size());
        String s = vr4.getIssues().get(0);
        assertTrue(s, s.contains("someFile1.bin") && s.contains("someFile2.bin"));
        assertFalse(s, s.contains("content.txt"));
        System.out.println(vr4.toString());
    }


    @Test
    public void testNpyValidation() throws Exception {

        File f = testDir.newFolder();

        //Test not existent file:
        File fNonExistent = new File("doesntExist.npy");
        ValidationResult vr0 = Nd4jValidator.validateNpyFile(fNonExistent);
        assertFalse(vr0.isValid());
        assertEquals("Numpy .npy File", vr0.getFormatType());
        assertTrue(vr0.getIssues().get(0), vr0.getIssues().get(0).contains("exist"));
        System.out.println(vr0.toString());

        //Test empty file:
        File fEmpty = new File(f, "empty.npy");
        fEmpty.createNewFile();
        assertTrue(fEmpty.exists());
        ValidationResult vr1 = Nd4jValidator.validateNpyFile(fEmpty);
        assertEquals("Numpy .npy File", vr1.getFormatType());
        assertFalse(vr1.isValid());
        assertTrue(vr1.getIssues().get(0), vr1.getIssues().get(0).contains("empty"));
        System.out.println(vr1.toString());

        //Test directory (not zip file)
        File directory = new File(f, "dir");
        boolean created = directory.mkdir();
        assertTrue(created);
        ValidationResult vr2 = Nd4jValidator.validateNpyFile(directory);
        assertEquals("Numpy .npy File", vr2.getFormatType());
        assertFalse(vr2.isValid());
        assertTrue(vr2.getIssues().get(0), vr2.getIssues().get(0).contains("directory"));
        System.out.println(vr2.toString());

        //Test non-numpy format:
        File fText = new File(f, "text.txt");
        FileUtils.writeStringToFile(fText, "Not a numpy .npy file", StandardCharsets.UTF_8);
        ValidationResult vr3 = Nd4jValidator.validateNpyFile(fText);
        assertEquals("Numpy .npy File", vr3.getFormatType());
        assertFalse(vr3.isValid());
        String s = vr3.getIssues().get(0);
        assertTrue(s, s.contains("npy") && s.toLowerCase().contains("numpy") && s.contains("corrupt"));
        System.out.println(vr3.toString());

        //Test corrupted npy format:
        File fValid = new ClassPathResource("numpy_arrays/arange_3,4_float32.npy").getFile();
        byte[] numpyBytes = FileUtils.readFileToByteArray(fValid);
        for( int i=0; i<30; i++ ){
            numpyBytes[i] = 0;
        }
        File fCorrupt = new File(f, "corrupt.npy");
        FileUtils.writeByteArrayToFile(fCorrupt, numpyBytes);

        ValidationResult vr4 = Nd4jValidator.validateNpyFile(fCorrupt);
        assertEquals("Numpy .npy File", vr4.getFormatType());
        assertFalse(vr4.isValid());
        s = vr4.getIssues().get(0);
        assertTrue(s, s.contains("npy") && s.toLowerCase().contains("numpy") && s.contains("corrupt"));
        System.out.println(vr4.toString());


        //Test valid npy format:
        ValidationResult vr5 = Nd4jValidator.validateNpyFile(fValid);
        assertEquals("Numpy .npy File", vr5.getFormatType());
        assertTrue(vr5.isValid());
        assertNull(vr5.getIssues());
        assertNull(vr5.getException());
        System.out.println(vr4.toString());

    }


    @Override
    public char ordering() {
        return 'c';
    }
}
