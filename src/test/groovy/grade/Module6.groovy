package grade

import static org.junit.jupiter.api.Assertions.*
import static org.junit.jupiter.api.DynamicTest.*
import static org.junit.jupiter.api.DynamicContainer.*
import org.junit.jupiter.api.*
import java.util.stream.*
import java.lang.reflect.*

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Module6 {
	static final MAP_OPERATIONS	= 1_000,
				 RANDOM_SEED	= 2020_01
	
	static final LOG_TO_FILE	= true
	
	static double grade
	static model.HashMap subject
	static java.util.AbstractMap exemplar
	static java.util.Random RNG
	
	@BeforeAll
	static void setup() {
		grade = 0
		subject = null
		exemplar = new java.util.HashMap<String, Integer>()
		RNG = new Random(RANDOM_SEED)
	}
	
	@TestFactory
	@DisplayName('Compliance [Audit]')
	@Order(0)
	final compliance() {
		return IntStream.rangeClosed(1, 5).mapToObj({step ->
			if (step == 1) {
				dynamicTest('No-Argument Constructor', {
					try {
						final Class<?> clazz = Class.forName('structure.VolatileHashMap');
					}
					catch (NoSuchMethodException e) {
						fail('Expected a no-argument constructor, but documented constructor is missing');
					}
				})
			}
			else if (step == 2) {
				dynamicTest('Copy Constructor', {
					try {
						final Class<?> clazz = Class.forName('structure.VolatileHashMap');
					}
					catch (NoSuchMethodException e) {
						fail('Expected a copy constructor, but documented constructor is missing');
					}
				})
			}
			else if (step == 3) {
				dynamicTest('Class Instantiable', {
					try {
						subject = new structure.VolatileHashMap<String, Integer>()
					}
					catch (Exception e) {
						fail('Expected to instantiate a hash map, but the hash map constructor fails', e);
					}
				})
			}
			else if (step == 4) {
				dynamicTest('Original Class', {
					if (subject == null) fail('Depends on preceding test')
						
					assertFalse(
						subject instanceof java.util.AbstractMap,
						'Expected an original hash map class, but project subclasses built-in hash map'
					)
				})
			}
			else if (step == 5) {
				dynamicTest('No Forbidden Classes', {
					if (subject == null) fail('Depends on preceding test')
					
					final allowed = [] as Set,
						  forbidden = [] as Set;
			  
					final internal = ['structure'],
						  exempt = ['java.lang']
	
					def clazz = structure.VolatileHashMap.class;
					
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
			LOG_FILE = new PrintStream('m6_map.log')
			LOG_FILE?.println("Map map = new VolatileHashMap();")
		}
		
		RNG.doubles(MAP_OPERATIONS).mapToObj({ p -> 
			if      (p < 0.60) test('put', key(), val())
			else if (p < 0.75) test('get', key())
			else if (p < 0.90) test('remove', key())
			else if (p < 0.93) test('size')
			else if (p < 0.95) test('isEmpty')
			else if (p < 0.98) test('containsKey', key())
			else               test('containsValue', val())
				
			// TODO: add hashCode(), clear() tests
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
			
			grade += (1 / MAP_OPERATIONS) * 85
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
					
					grade += 5
				})
			}
			else if (step == 2) {
				dynamicTest("putAll(_) [entries=${subject.size()*4}]", {
					final batch = [:] as Map
					while (batch.size() < subject.size()*4)
						batch[key()] = val()
					
					exemplar.putAll(batch)
					subject.putAll(batch)
					
					assertEquals(
						exemplar.size(),
						subject.size(),
						'putAll(other_map) must yield correct results for size()'
					)
					
					grade += 5
				})
			}
			else if (step == 3) {
				dynamicTest("this.equals(_) ${stats()}", {
					assertTrue(
						((Object) subject).equals((Object) exemplar),
						'this.equals(other_map) must yield correct results; depends on this map\'s entrySet() via iterator()'
					)
					
					grade += 5
				})
			}
		})
	}
	
	@AfterAll
	static final void report() {
		System.out.println("[M6 PASSED ${Math.round(grade)}% OF WEIGHTED TESTS]")
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