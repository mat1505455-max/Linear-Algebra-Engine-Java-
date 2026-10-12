package memory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SharedMatrixTest {

    @Test
    @DisplayName("empty matrix should have length 0 and handle operation correctly")
    void testEmptyMatrix() {
        SharedMatrix matrix = new SharedMatrix();
        assertEquals(0, matrix.length(), "empty matrix should have length 0");
        
        // מקרה קצה: קריאת נתונים ממטריצה ריקה
        double[][] result = matrix.readRowMajor();
        assertEquals(0, result.length);
        
        // מקרה קצה: קבלת אוריינטציה ממטריצה ריקה (צריך לזרוק שגיאה)
        assertThrows(IllegalArgumentException.class, () -> matrix.getOrientation());
    }

    @Test
    @DisplayName("correctly load and read row major matrix")
    void testLoadAndReadRowMajor() {
        double[][] input = {
            {1.0, 2.0, 3.0},
            {4.0, 5.0, 6.0}
        };
        SharedMatrix matrix = new SharedMatrix(input);

        assertEquals(2, matrix.length());
        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation());
        
        double[][] output = matrix.readRowMajor();
        
        // בדיקת תוכן
        assertArrayEquals(input[0], output[0]);
        assertArrayEquals(input[1], output[1]);
        
        // בדיקה שהמערכים הועתקו (Deep Copy ב-SharedVector)
        input[0][0] = 99.0;
        assertNotEquals(input[0][0], matrix.get(0).get(0), "matrix should encapsulate its data");
    }

    @Test
    @DisplayName("load column major matrix correctly")
    void testLoadColumnMajor() {
        double[][] input = {
            {1.0, 1.0},
            {2.0, 2.0}
        };
        SharedMatrix matrix = new SharedMatrix();
        matrix.loadColumnMajor(input);

        assertEquals(2, matrix.length());
        assertEquals(VectorOrientation.COLUMN_MAJOR, matrix.getOrientation());
    }

    @Test
    @DisplayName("exception on get with invalid index")
    void testGetOutOfBounds() {
        double[][] data = {{1.0, 2.0}};
        SharedMatrix matrix = new SharedMatrix(data);

        assertThrows(IndexOutOfBoundsException.class, () -> matrix.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> matrix.get(1));
        assertDoesNotThrow(() -> matrix.get(0));
    }

    @Test
    @DisplayName("בדיקת מניעת טעינת מטריצה משוננת")
    void testRejectJaggedMatrix() {
        double[][] jagged = {
        {1.0, 2.0, 3.0},
        {4.0} // שורה קצרה מדי
    };
    
    SharedMatrix matrix = new SharedMatrix();
  
    assertThrows(IllegalArgumentException.class, () -> {
        matrix.loadRowMajor(jagged);
        }, "should throw exception for jagged matrix in row major loadiong");
    }


    @Test
    @DisplayName("test locking logic to avoid deadlocks")
    void testLockingLogic() {
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}};
        SharedMatrix matrix = new SharedMatrix(data);
        
        // readRowMajor מפעיל נעילות קריאה על כל הוקטורים
        // get(j) בתוך הלולאה מפעיל שוב נעילת קריאה (נעילה חוזרת - Reentrant)
        assertDoesNotThrow(() -> {
            double[][] res = matrix.readRowMajor();
            assertEquals(2, res.length);
        });
    }

    @Test
    @DisplayName("test single element matrix")
    void testSingleElementMatrix() {
        double[][] data = {{42.0}};
        SharedMatrix matrix = new SharedMatrix(data);
        
        assertEquals(1, matrix.length());
        assertEquals(42.0, matrix.get(0).get(0));
        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation());
    }
}