package audit

import static org.junit.jupiter.api.Assertions.*
import static org.junit.jupiter.api.DynamicTest.*
import static org.junit.jupiter.api.DynamicContainer.*
import org.junit.jupiter.api.*
import java.util.stream.*
import java.lang.reflect.*
import java.nio.file.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Module8 {
	static final MAP_OPERATIONS	= 1_000,
				 RANDOM_SEED	= 2020_01
	
	static final LOG_TO_FILE	= true
	
	static model.HashMap subject
	static java.util.AbstractMap exemplar
	static java.util.Random RNG
	
	@BeforeAll
	static void setup() {
		subject = null
		exemplar = new java.util.HashMap<Object, List<Object>>()
		RNG = new Random(RANDOM_SEED)
	}
	
	@TestFactory
	@DisplayName('Compliance [Audit]')
	@Order(0)
	final compliance() {
		return IntStream.rangeClosed(1, 6).mapToObj({step ->
			if (step == 1) {
				dynamicTest('Page-Only Constructor', {
					try {
						final Class<?> clazz = Class.forName('structure.PersistentHashMap');
						final Class<?> ipage = Class.forName('model.Page');
						clazz.getDeclaredConstructor(ipage);
					}
					catch (NoSuchMethodException e) {
						fail('Expected a page-only constructor, but documented constructor is missing');
					}
				})
			}
			else if (step == 2) {
				dynamicTest('Copy Constructor', {
					try {
						final Class<?> clazz = Class.forName('structure.PersistentHashMap');
						final Class<?> ipage = Class.forName('model.Page');
						final Class<?> imap = Class.forName('model.HashMap');
						clazz.getDeclaredConstructor(ipage, imap);
					}
					catch (NoSuchMethodException e) {
						fail('Expected a copy constructor, but documented constructor is missing');
					}
				})
			}
			else if (step == 3) {
				dynamicTest('Data Folder Accessible', {
					try {
						if (Files.notExists(Paths.get('data')))
							Files.createDirectory(Paths.get('data'));
						if (Files.notExists(Paths.get('data', 'audit')))
							Files.createDirectory(Paths.get('data', 'audit'));
						else
							Files.walk(Paths.get('data', 'audit'))
								.skip(1)
								.sorted(Comparator.reverseOrder())
								.forEach({path -> Files.delete(path)});
					}
					catch (Exception e) {
						fail('Expected accessible output folder, but access fails', e);
					}
				})
			}
			else if (step == 4) {
				dynamicTest('Class Instantiable', {
					try {
						def path = Paths.get('data', 'audit', "m8_page.bin");
						subject = new structure.PersistentHashMap(new structure.SimplePage(
							path,
							['string', 'integer'],
							0
						))
					}
					catch (Exception e) {
						fail('Expected to instantiate a hash map, but the hash map constructor fails', e);
					}
				})
			}
			else if (step == 5) {
				dynamicTest('Original Class', {
					if (subject == null) fail('Depends on preceding test')
						
					assertFalse(
						subject instanceof java.util.AbstractMap,
						'Expected an original hash map class, but project subclasses built-in hash map'
					)
				})
			}
			else if (step == 6) {
				dynamicTest('No Forbidden Classes', {
					if (subject == null) fail('Depends on preceding test')
					
					final allowed = [] as Set,
						  forbidden = [] as Set;
			  
					final internal = ['structure'],
						  exempt = ['java.lang']
	
					def clazz = structure.PersistentHashMap.class;
					
					while (clazz != null) {
						for (Field f: clazz.getFields() + clazz.getDeclaredFields()) {
							f.setAccessible(true)
							if (f.get(subject) != null) {
								def used = f.get(subject).getClass();
								while (used.isArray())
									used = used.getComponentType()
								if (!used.isPrimitive() && !used.isInterface()) {
									if (!(used.getPackage()?.getName() in exempt + internal))
										forbidden.add(used)
									else if (!used.getPackage()?.getName() in internal)
										allowed.add(used)
								}
							}
							f.setAccessible(false)
						}
						clazz = clazz.getSuperclass();
					}
	
					if (allowed.size() + forbidden.size())
						System.err.println('Map fields use external classes:')
					allowed.each({System.err.println("$it (allowed)")})
					forbidden.each({System.err.println("$it (forbidden)")})
	
					if (forbidden)
						fail("Map fields use forbidden ${forbidden.join(', ')}.")
				})
			}
		})
	}
	
	static PrintStream LOG_FILE
	
	@TestFactory
	@DisplayName('Battery [85%]')
	@Order(1)
	final battery() {
		if (subject == null) fail('Depends on compliance tests')
		
		if (LOG_TO_FILE) {
			LOG_FILE = new PrintStream('m8_map.log')
			LOG_FILE?.println("Map map = new PersistentHashMap();")
			// TODO pass new Page
		}
		
		RNG.doubles(MAP_OPERATIONS).mapToObj({ p -> 
			if      (p < 0.60) {def k = key(); test('put', k, [k, val()])}
			else if (p < 0.75) test('get', key())
			else if (p < 0.95) test('remove', key())
			else			   test('size')
		})
	}
	
	static final test(def method, def ...args) {
		final call = "$method(${args ? args.inspect()[1..-2] : ''})"
		
		return dynamicTest(method in ['size', 'isEmpty'] ? "$call ${stats()}" : call, {
			if (LOG_TO_FILE) LOG_FILE?.println("map.$call;".replace("'", '"'))
			
			assertEquals(
				exemplar."$method"(*args),
				subject."$method"(*args),
				"$call must yield correct results"
			)
		})
	}
	
	@TestFactory
	@DisplayName('Robustness [15%]')
	@Order(2)
	final robustness() {
		if (subject == null) fail('Depends on compliance tests')
			
		return IntStream.rangeClosed(1, 3).mapToObj({step -> 
			if (step == 1) {
				dynamicTest("_.equals(this) ${stats()}", {
					assertTrue(
						((Object) exemplar).equals((Object) subject),
						'other_map.equals(this) must yield correct results; depends on this map\'s size() and containsKey()'
					)
				})
			}
			else if (step == 2) {
				dynamicTest("putAll(_) [entries=${subject.size()*4}]", {
					final batch = [:] as Map
					while (batch.size() < subject.size()*4) {
						def k = key()
						batch[k] = [k, val()]
					}
					
					exemplar.putAll(batch)
					subject.putAll(batch)
					
					assertEquals(
						exemplar.size(),
						subject.size(),
						'putAll(other_map) must yield correct results for size()'
					)
				})
			}
			else if (step == 3) {
				dynamicTest("this.equals(_) ${stats()}", {
					assertTrue(
						((Object) subject).equals((Object) exemplar),
						'this.equals(other_map) must yield correct results; depends on this map\'s entrySet() via iterator()'
					)
				})
			}
		})
	}
	
	static final KEY_ALPHABET = '012456789abcdef'
	static final key() {
		RNG.ints((long) (Math.abs(RNG.nextGaussian())*1.5+1)).mapToObj({i -> KEY_ALPHABET[i % KEY_ALPHABET.size()]}).collect(Collectors.joining())
	}
	
	static final VALUE_RANGE = 1_000
	static final val() {
		RNG.nextInt(VALUE_RANGE)
	}
	
	static final stats() {
		def buff = new StringBuffer('[n=')
		
		try {
			buff.append(subject.size())
		}
		catch (Exception e) {
			buff.append('?')
		}
		
		buff.append(', \u03B1=')
		
		try {
			buff.append((int) (subject.loadFactor()*1000) / 1000)
		}
		catch (Exception e) {
			buff.append('?')
		}
		
		buff.append(']')

		return buff.toString()
	}
}