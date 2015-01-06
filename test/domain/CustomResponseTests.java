package domain;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by jasbuber on 2014-12-22.
 */
public class CustomResponseTests {

    @Test
    public void testAddPoints(){
        CustomResponse response = new CustomResponse();
        assertTrue(response.getRewardedPoints() == 0);
        assertTrue(response.getPoints().isEmpty());

        response.addPoints(10);

        assertTrue(response.getRewardedPoints() == 10);
        assertFalse(response.getPoints().isEmpty());
    }

    @Test
    public void testDefaultStatus(){
        CustomResponse response = new CustomResponse();
        assertTrue(response.getStatus().equals(CustomResponse.ResponseStatus.success));
    }
}
