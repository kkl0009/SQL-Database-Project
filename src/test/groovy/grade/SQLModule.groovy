package grade

import static org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*

import core.Database
import model.Table
import model.Response
import structure.SimpleResponse

abstract class SQLModule {
	static final SERIALIZE_MODE = false;
	
	static Database DB = new Database()
	
	static int passed_queries = 0
	static int total_queries = 0
	
	static String module_tag
	
	static List query_data,
			computed_schemas,
			computed_states,
			stored_schemas,
			stored_states
	
	static data() {
		def empty = {[null]*query_data.size()},
			pad = {it + [null]*(query_data.size()-it.size())}
		[
			query_data,
			computed_schemas = computed_schemas ? pad(computed_schemas) : empty(),
			computed_states = computed_states ? pad(computed_states) : empty(),
			stored_schemas = stored_schemas ? pad(stored_schemas) : empty(),
			stored_states = stored_states ? pad(stored_states) : empty()
		].transpose()*.flatten()*.toArray()
	}
	
	@DisplayName('Queries')
	@ParameterizedTest(name = '[{index}] {1}')
	@MethodSource('data')
	void testQuery(
		boolean query_success,
		String query_code,
		String test_reason,
		Map computed_schema,
		Map computed_state,
		Map stored_schema,
		Map stored_state
	) {
		total_queries++;
		
		System.out.println(query_code)
		
		def queries = query_code.split(';'),
			query_count = queries.size()
		
		def on_table = (queries[-1].strip() =~ /^\w+\s+\w+\s+([a-z][a-z0-9_]*)\b/),
			stored_table_name = on_table.size() == 1 ? on_table[0][1] : null
		
		def responses
		try {
			responses = DB.interpret(query_code)
		}
		catch (Exception e) {
			fail('Interpreter must not throw exceptions', e);
		}
		
		def last
		try {
			last = responses[-1]
		}
		catch (Exception e) {
			fail('Interpreter must return non-null and non-empty list of responses');
		}
		def last_table = last?.getTable()
		
		if (SERIALIZE_MODE) {
			def ser = {it.inspect()?.replaceAll(/(true|false|null):/, '($1):') ?: 'null'}
			
			computed_schemas[total_queries-1] = 
				ser(last_table ? [
					'table_name': last_table.getTableName(),
					'column_names': last_table.getColumnNames(),
					'column_types': last_table.getColumnTypes(),
					'primary_index': last_table.getPrimaryIndex()
				] : null)
				
			computed_states[total_queries-1] =
				ser(last_table?.getState())
			
			stored_schemas[total_queries-1] =
				ser(DB.getTables()?.get(stored_table_name) ? [
					'table_name': DB.getTables()?.get(stored_table_name)?.getTableName(),
					'column_names': DB.getTables()?.get(stored_table_name)?.getColumnNames(),
					'column_types': DB.getTables()?.get(stored_table_name)?.getColumnTypes(),
					'primary_index': DB.getTables()?.get(stored_table_name)?.getPrimaryIndex()
				] : null)
			
			stored_states[total_queries-1] =
				ser(DB.getTables()?.get(stored_table_name)?.getState())
			
			return
		}
		
		assertEquals(
			query_count,
			responses?.count({it != null}),
			String.format(
				'%s returned list with wrong number of non-null responses,',
				query_count == 1 ? 'Query' : 'Script'
			)
		)
		
		assertEquals(
			query_count == 1 ? query_success : [true]*(query_count-1) + [query_success],
			query_count == 1 ? last.getSuccess() : responses.collect{it.getSuccess()},
			String.format(
				'%s %s was expected to %s, reason: <%s>, message: <%s>,',
				query_success ? 'Valid' : 'Invalid',
				query_count == 1 ? 'query' : 'script',
				(query_count == 1
					? (query_success ? 'succeed' : 'fail')
					: (query_success ? 'succeed for all queries' : 'fail only on last query')),
				test_reason ?: 'none provided',
				last.getMessage() ?: 'none returned'
			)
		)
		
		if (computed_schema) {
			assertEquals(
				computed_schema.get("table_name"),
				last_table?.getTableName(),
				String.format(
					'%s returned <computed> table with incorrect table name,',
					query_count == 1 ? 'Query' : 'Last query of script'
				)
			)
			
			assertEquals(
				computed_schema.get("column_names"),
				last_table?.getColumnNames(),
				String.format(
					'%s returned <computed> table with incorrect column names,',
					query_count == 1 ? 'Query' : 'Last query of script'
				)
			)
			
			assertEquals(
				computed_schema.get("column_types"),
				last_table?.getColumnTypes(),
				String.format(
					'%s returned <computed> table with incorrect column types,',
					query_count == 1 ? 'Query' : 'Last query of script'
				)
			)
			
			assertEquals(
				computed_schema.get("primary_index"),
				last_table?.getPrimaryIndex(),
				String.format(
					'%s returned <computed> table with incorrect primary index,',
					query_count == 1 ? 'Query' : 'Last query of script'
				)
			)
		}
		
		if (computed_state) assertEquals(
			computed_state,
			last?.getTable()?.getState(),
			String.format(
				'%s returned <computed> table with incorrect rows,',
				query_count == 1 ? 'Query' : 'Last query of script'
			)
		)
		
		if (stored_schema) {
			assertEquals(
				stored_schema.get("table_name"),
				DB.getTables()?.get(stored_table_name)?.getTableName(),
				String.format(
					'%s caused <stored> table to have incorrect table name,',
					query_count == 1 ? 'Query' : 'Last query of script'
				)
			)
			
			assertEquals(
				stored_schema.get("column_names"),
				DB.getTables()?.get(stored_table_name)?.getColumnNames(),
				String.format(
					'%s caused <stored> table to have incorrect column names,',
					query_count == 1 ? 'Query' : 'Last query of script'
				)
			)
			
			assertEquals(
				stored_schema.get("column_types"),
				DB.getTables()?.get(stored_table_name)?.getColumnTypes(),
				String.format(
					'%s caused <stored> table to have incorrect column types,',
					query_count == 1 ? 'Query' : 'Last query of script'
				)
			)
			
			assertEquals(
				stored_schema.get("primary_index"),
				DB.getTables()?.get(stored_table_name)?.getPrimaryIndex(),
				String.format(
					'%s caused <stored> table to have incorrect primary index,',
					query_count == 1 ? 'Query' : 'Last query of script'
				)
			)
		}
		
		if (stored_state) assertEquals(
			stored_state,
			DB.getTables()?.get(stored_table_name)?.getState(),
			String.format(
				'%s caused <stored> table to have incorrect rows,',
				query_count == 1 ? 'Query' : 'Last query of script'
			)
		)
		
		passed_queries++;
	}
	
	@AfterAll
	static void report() {
		final int rate = Math.ceil(passed_queries / (double) total_queries * 100)
		System.out.println(
			"[$module_tag PASSED $rate% OF UNIT TESTS]",
		)
		
		if (SERIALIZE_MODE) {
			System.out.println()
			
			System.err.println('\t\tcomputed_schemas = [')
			for (String s: computed_schemas)
				System.err.println("\t\t\t$s,")
			System.err.println('\t\t]')
			
			System.err.println()
			
			System.err.println('\t\tcomputed_states = [')
			for (String s: computed_states)
				System.err.println("\t\t\t$s,")
			System.err.println('\t\t]')
			
			if (stored_schemas.count({it != null.inspect()}) > 0) {
				System.err.println()
				
				System.err.println('\t\tstored_schemas = [')
				for (String s: stored_schemas)
					System.err.println("\t\t\t$s,")
				System.err.println('\t\t]')
			}
			
			if (stored_states.count({it != null.inspect()}) > 0) {
				System.err.println()
				
				System.err.println('\t\tstored_states = [')
				for (String s: stored_states)
					System.err.println("\t\t\t$s,")
				System.err.println('\t\t]')
			}
		}
		
		try {
			DB.close()
		}
		catch (Exception e) {}
	}
}