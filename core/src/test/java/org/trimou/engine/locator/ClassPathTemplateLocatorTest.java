package org.trimou.engine.locator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class ClassPathTemplateLocatorTest extends PathTemplateLocatorTest {

	@Test
	public void testLocator() throws IOException {

		TemplateLocator locator = new ClassPathTemplateLocator(1, "locator/file", "foo");

		Set<String> names = locator.getAllAvailableNames();
		assertEquals(2, names.size());
		assertTrue(names.contains("index"));
		assertTrue(names.contains("home"));

		String index = read(locator.locate("index"));
		assertEquals("{{foo}}", index);

		String home = read(locator.locate("home"));
		assertEquals("bar", home);
	}

	@Test
	public void testLocatorNoSuffix() throws IOException {

		TemplateLocator locator = new ClassPathTemplateLocator(1,
				"locator/file");

		Set<String> names = locator.getAllAvailableNames();
		assertEquals(3, names.size());
		assertTrue(names.contains("index.foo"));
		assertTrue(names.contains("home.foo"));
		assertTrue(names.contains("detail.html"));

		assertEquals("{{foo}}", read(locator.locate("index.foo")));
		assertEquals("bar", read(locator.locate("home.foo")));
		assertEquals("<html/>", read(locator.locate("detail.html")));
	}

}
