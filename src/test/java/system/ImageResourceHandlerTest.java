package system;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import visualizer.ImageResourceHandler;

import java.util.HashMap;

class ImageResourceHandlerTest {
	static public ImageResourceHandler imageResourceHandler;

	@BeforeAll
	static void setUp(){
		imageResourceHandler = ImageResourceHandler.getInstance();
	}
	@Test
	void ResourceLoadTest(){
		HashMap<String,String> internal_map = imageResourceHandler.ImageResources;
		Assertions.assertTrue(internal_map.size()>0);
	}
	@Test
	void ResourceURLNonNullTest(){
		HashMap<String,String> internal_map = imageResourceHandler.ImageResources;
		for (String id:internal_map.keySet()){
			Assertions.assertNotNull(internal_map.get(id));
		}
	}
	@Test
	void ResourceNonNullTest(){
		HashMap<String,String> internal_map = imageResourceHandler.ImageResources;
		for (String id:internal_map.keySet()){
			Assertions.assertNotNull(imageResourceHandler.getImage(id));
		}
	}
	@Test
	void ResourceNullTest(){
		Assertions.assertNull(imageResourceHandler.getImage("invalid id"));
	}
	@Test
	void ResourceNullTest2(){
		HashMap<String,String> internal_map = imageResourceHandler.ImageResources;
		Assertions.assertNull(internal_map.size()<0);
	}
}
