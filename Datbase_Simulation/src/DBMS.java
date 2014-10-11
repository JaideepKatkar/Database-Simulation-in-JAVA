import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CS 267 - Project - Implements create index, drop index, list table, and
 * exploit the index in select statements.
 */
public class DBMS {
	private static final String COMMAND_FILE_LOC = ".\\src\\Commands.txt";
	private static final String OUTPUT_FILE_LOC = ".\\src\\Output.txt";

	private static final String TABLE_FOLDER_NAME = ".\\src\\tables";
	private static final String TABLE_FILE_EXT = ".tab";
	private static final String INDEX_FILE_EXT = ".idx";

	public static boolean ASC = true;
	public static boolean DESC = false;
	private ArrayList<Integer> table1Card;
	private ArrayList<String>  table2Card;
	private ArrayList<Integer> table1Cardn;
	private ArrayList<String>  table2Cardn;
	private ArrayList<String> tlist;
	private ArrayList<Integer>  tcollist;
	private ArrayList<Integer> AColName;
	private ArrayList<Integer> AIndName;
	private ArrayList<Integer> APPos;
	private ArrayList<String> ATabName;
	private ArrayList<Integer> AColName1;
	private ArrayList<Integer> AIndName1;
	private ArrayList<Integer> APPos1;
	private ArrayList<String> ATabName1;
	private ArrayList<Double> AFFilter;
	public static boolean flag_index_tab1     =false;
	public static boolean flag_index_tab2     =false;
	public static boolean flag_index_only     =false;
	public static boolean flag_In_list        =false;
	public static boolean flag_query_has_orderby  =false;
	public static boolean flag_query_has_where    =false;
	public static boolean flag_where_with_candidate=false;
	public static boolean flag_prefetch       =true;
	public static boolean flag_SortcOrderby   =false;	
	public static boolean flag_two_tables     =false;		
    public static int     matchcolumns        =0    ;
	public static String  access_name         ="*" ;
	public static int     num_of_distintict_cols_in_where   = 0;
	private DbmsPrinter out;
	private ArrayList<Table> tables;

	public DBMS() {
		tables = new ArrayList<Table>();
		table1Card = new ArrayList<Integer>();
		table2Card = new ArrayList<String>();
		tlist = new ArrayList<String>();
		tcollist = new ArrayList<Integer>();
		AColName = new ArrayList<Integer>();
		AIndName = new ArrayList<Integer>();
		APPos = new ArrayList<Integer>();
		ATabName = new ArrayList<String>();
		AFFilter = new ArrayList<Double>();
		AColName1 = new ArrayList<Integer>();
		AIndName1 = new ArrayList<Integer>();
		APPos1 = new ArrayList<Integer>();
		ATabName1 = new ArrayList<String>();
		table1Cardn = new ArrayList<Integer>();
		table2Cardn = new ArrayList<String>();
	}

	/**
	 * Main method to run the DBMS engine.
	 * 
	 * @param args
	 *            arg[0] is input file, arg[1] is output file.
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		DBMS db = new DBMS();
		db.out = new DbmsPrinter();
		Scanner in = null;
		
		try {
			// set input file
			if (args.length > 0) {
				in = new Scanner(new File(args[0]));
			} else {
				in = new Scanner(new File(COMMAND_FILE_LOC));
			}
			
			// set output files
			if (args.length > 1) {
				db.out.addPrinter(args[1]);
			} else {
				db.out.addPrinter(OUTPUT_FILE_LOC);
			}

			// Load data to memory
			db.loadTables();

			// Go through each line in the Command.txt file
			while (in.hasNextLine()) {
				String sql = in.nextLine();
				
				StringTokenizer tokenizer = new StringTokenizer(sql);

				// Evaluate the SQL statement
				if (tokenizer.hasMoreTokens()) {
					String command = tokenizer.nextToken();
					if (command.equalsIgnoreCase("CREATE")) {
						if (tokenizer.hasMoreTokens()) {
							command = tokenizer.nextToken();
							if (command.equalsIgnoreCase("TABLE")) {
								db.createTable(sql, tokenizer);
							} else if (command.equalsIgnoreCase("UNIQUE")) {
								command = tokenizer.nextToken();
								if (command.equalsIgnoreCase("INDEX")) {
									// TODO your PART 1 code goes here
									//db.CreateTableIndex(sql, tokenizer, true);
								} else {
									throw new DbmsError(
											"Invalid CREATE UNIQUE " + command
													+ " statement. '" + sql
													+ "'.");
								}
							} else if (command.equalsIgnoreCase("INDEX")) {
								// TODO your PART 1 code goes here
								/*db.CreateTableIndex(sql, tokenizer, false);*/
							} else {
								throw new DbmsError("Invalid CREATE " + command
										+ " statement. '" + sql + "'.");
							}
						} else {
							throw new DbmsError("Invalid CREATE statement. '"
									+ sql + "'.");
						}
					} else if (command.equalsIgnoreCase("INSERT")) {
						db.insertInto(sql, tokenizer);
					} else if (command.equalsIgnoreCase("DROP")) {
						if (tokenizer.hasMoreTokens()) {
							command = tokenizer.nextToken();
							if (command.equalsIgnoreCase("TABLE")) {
								db.dropTable(sql, tokenizer);
							} else if (command.equalsIgnoreCase("INDEX")) {
								db.DropTableIndex(sql, tokenizer);
							} else {
								throw new DbmsError("Invalid DROP " + command
										+ " statement. '" + sql + "'.");
							}
						} else {
							throw new DbmsError("Invalid DROP statement. '"
									+ sql + "'.");
						}
					} else if (command.equalsIgnoreCase("RUNSTATS")) {

						String tableName = tokenizer.nextToken();

						if (!tokenizer.nextElement().equals(";")) {
							throw new NoSuchElementException();
						}

						// Check if there are more tokens
						if (tokenizer.hasMoreTokens()) {
							throw new NoSuchElementException();
						}

						//db.CallRunStats(tableName);
					//	db.outRunTablestats(tableName);

					} else if (command.equalsIgnoreCase("SELECT")) {
						// TODO your PART 2 code goes here
						//String tabN = tokenizer.nextToken() ; 
						
						db.selectsqlscan(sql);
						//db.tableIndex(tabN);
						
					} else if (command.equalsIgnoreCase("--")) {
						// Ignore this command as a comment
					} else if (command.equalsIgnoreCase("COMMIT")) {
						try {
							// Check for ";"
							if (!tokenizer.nextElement().equals(";")) {
								throw new NoSuchElementException();
							}

							// Check if there are more tokens
							if (tokenizer.hasMoreTokens()) {
								throw new NoSuchElementException();
							}

							// Save tables to files
							for (Table table : db.tables) {
								db.storeTableFile(table);
							}
						} catch (NoSuchElementException ex) {
							throw new DbmsError("Invalid COMMIT statement. '"
									+ sql + "'.");
						}
					} else {
						throw new DbmsError("Invalid statement. '" + sql + "'.");
					}
				}
			}

			// Save tables to files
			for (Table table : db.tables) {
				db.storeTableFile(table);
			}
		} catch (DbmsError ex) {
			db.out.println("DBMS ERROR:  " + ex.getMessage());
			ex.printStackTrace();
		} catch (Exception ex) {
			db.out.println("JAVA ERROR:  " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			// clean up
			try {
				in.close();
			} catch (Exception ex) {
			}

			try {
				db.out.cleanup();
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * Loads tables to memory
	 * 
	 * @throws Exception
	 */
	private void loadTables() throws Exception {
		// Get all the available tables in the "tables" directory
		File tableDir = new File(TABLE_FOLDER_NAME);
		if (tableDir.exists() && tableDir.isDirectory()) {
			for (File tableFile : tableDir.listFiles()) {
				// For each file check if the file extension is ".tab"
				String tableName = tableFile.getName();
				int periodLoc = tableName.lastIndexOf(".");
				String tableFileExt = tableName.substring(tableName
						.lastIndexOf(".") + 1);
				if (tableFileExt.equalsIgnoreCase("tab")) {
					// If it is a ".tab" file, create a table structure
					Table table = new Table(tableName.substring(0, periodLoc));
					Scanner in = new Scanner(tableFile);

					try {
						// Read the file to get Column definitions
						int numCols = Integer.parseInt(in.nextLine());

						for (int i = 0; i < numCols; i++) {
							StringTokenizer tokenizer = new StringTokenizer(
									in.nextLine());
							String name = tokenizer.nextToken();
							String type = tokenizer.nextToken();
							boolean nullable = Boolean.parseBoolean(tokenizer
									.nextToken());
							switch (type.charAt(0)) {
							case 'C':
								table.addColumn(new Column(i + 1, name,
										Column.ColType.CHAR, Integer
												.parseInt(type.substring(1)),
										nullable));
								break;
							case 'I':
								table.addColumn(new Column(i + 1, name,
										Column.ColType.INT, 4, nullable));
								break;
							default:
								break;
							}
						}

						// Read the file for index definitions
						int numIdx = Integer.parseInt(in.nextLine());
						for (int i = 0; i < numIdx; i++) {
							StringTokenizer tokenizer = new StringTokenizer(
									in.nextLine());
							Index index = new Index(tokenizer.nextToken());
							index.setIsUnique(Boolean.parseBoolean(tokenizer
									.nextToken()));

							int idxColPos = 1;
							while (tokenizer.hasMoreTokens()) {
								String colDef = tokenizer.nextToken();
								Index.IndexKeyDef def = index.new IndexKeyDef();
								def.idxColPos = idxColPos;
								def.colId = Integer.parseInt(colDef.substring(
										0, colDef.length() - 1));
								switch (colDef.charAt(colDef.length() - 1)) {
								case 'A':
									def.descOrder = false;
									break;
								case 'D':
									def.descOrder = true;
									break;
								default:
									break;
								}

								index.addIdxKey(def);
								idxColPos++;
							}

							table.addIndex(index);
							loadIndex(table, index);
						}

						// Read the data from the file
						int numRows = Integer.parseInt(in.nextLine());
						for (int i = 0; i < numRows; i++) {
							table.addData(in.nextLine());
						}
						
						// Read RUNSTATS from the file
						while(in.hasNextLine()) {
							String line = in.nextLine();
							StringTokenizer toks = new StringTokenizer(line);
							if(toks.nextToken().equals("STATS")) {
								String stats = toks.nextToken();
								if(stats.equals("TABCARD")) {
									table.setTableCard(Integer.parseInt(toks.nextToken()));
								} else if (stats.equals("COLCARD")) {
									Column col = table.getColumns().get(Integer.parseInt(toks.nextToken()));
									col.setColCard(Integer.parseInt(toks.nextToken()));
									col.setHiKey(toks.nextToken());
									col.setLoKey(toks.nextToken());
								} else {
									throw new DbmsError("Invalid STATS.");
								}
							} else {
								throw new DbmsError("Invalid STATS.");
							}
						}
						
						
						
						
					} catch (DbmsError ex) {
						throw ex;
					} catch (Exception ex) {
						throw new DbmsError("Invalid table file format.");
					} finally {
						in.close();
					}
					tables.add(table);
				}
			}
		} else {
			throw new FileNotFoundException(
					"The system cannot find the tables directory specified.");
		}
	}

	/**
	 * Loads specified table to memory
	 * 
	 * @throws DbmsError
	 */
	private void loadIndex(Table table, Index index) throws DbmsError {
		try {
			Scanner in = new Scanner(new File(TABLE_FOLDER_NAME,
					table.getTableName() + index.getIdxName() + INDEX_FILE_EXT));
			String def = in.nextLine();
			String rows = in.nextLine();

			while (in.hasNext()) {
				String line = in.nextLine();
				Index.IndexKeyVal val = index.new IndexKeyVal();
				val.rid = Integer.parseInt(new StringTokenizer(line)
						.nextToken());
				val.value = line.substring(line.indexOf("'") + 1,
						line.lastIndexOf("'"));
				index.addKey(val);
			}
			in.close();
		} catch (Exception ex) {
			throw new DbmsError("Invalid index file format.");
		}
	}

	/**
	 * CREATE TABLE
	 * <table name>
	 * ( <col name> < CHAR ( length ) | INT > <NOT NULL> ) ;
	 * 
	 * @param sql
	 * @param tokenizer
	 * @throws Exception
	 */
	private void createTable(String sql, StringTokenizer tokenizer)
			throws Exception {
		try {
			// Check the table name
			String tok = tokenizer.nextToken().toUpperCase();
			if (Character.isAlphabetic(tok.charAt(0))) {
				// Check if the table already exists
				
				
				
				for (Table tab : tables) {
					if (tab.getTableName().equals(tok)) {
						throw new DbmsError("Table " + tok
								+ "already exists. '" + sql + "'.");
					}
				}

				// Create a table instance to store data in memory
				Table table = new Table(tok.toUpperCase());

				// Check for '('
				tok = tokenizer.nextToken();
				if (tok.equals("(")) {
					// Look through the column definitions and add them to the
					// table in memory
					boolean done = false;
					int colId = 1;
					while (!done) {
						tok = tokenizer.nextToken();
						if (Character.isAlphabetic(tok.charAt(0))) {
							String colName = tok;
							Column.ColType colType = Column.ColType.INT;
							int colLength = 4;
							boolean nullable = true;

							tok = tokenizer.nextToken();
							if (tok.equalsIgnoreCase("INT")) {
								// use the default Column.ColType and colLength

								// Look for NOT NULL or ',' or ')'
								tok = tokenizer.nextToken();
								if (tok.equalsIgnoreCase("NOT")) {
									// look for NULL after NOT
									tok = tokenizer.nextToken();
									if (tok.equalsIgnoreCase("NULL")) {
										nullable = false;
									} else {
										throw new NoSuchElementException();
									}

									tok = tokenizer.nextToken();
									if (tok.equals(",")) {
										// Continue to the next column
									} else if (tok.equalsIgnoreCase(")")) {
										done = true;
									} else {
										throw new NoSuchElementException();
									}
								} else if (tok.equalsIgnoreCase(",")) {
									// Continue to the next column
								} else if (tok.equalsIgnoreCase(")")) {
									done = true;
								} else {
									throw new NoSuchElementException();
								}
							} else if (tok.equalsIgnoreCase("CHAR")) {
								colType = Column.ColType.CHAR;

								// Look for column length
								tok = tokenizer.nextToken();
								if (tok.equals("(")) {
									tok = tokenizer.nextToken();
									try {
										colLength = Integer.parseInt(tok);
									} catch (NumberFormatException ex) {
										throw new DbmsError(
												"Invalid table column length for "
														+ colName + ". '" + sql
														+ "'.");
									}

									// Check for the closing ')'
									tok = tokenizer.nextToken();
									if (!tok.equals(")")) {
										throw new DbmsError(
												"Invalid table column definition for "
														+ colName + ". '" + sql
														+ "'.");
									}

									// Look for NOT NULL or ',' or ')'
									tok = tokenizer.nextToken();
									if (tok.equalsIgnoreCase("NOT")) {
										// Look for NULL after NOT
										tok = tokenizer.nextToken();
										if (tok.equalsIgnoreCase("NULL")) {
											nullable = false;

											tok = tokenizer.nextToken();
											if (tok.equals(",")) {
												// Continue to the next column
											} else if (tok
													.equalsIgnoreCase(")")) {
												done = true;
											} else {
												throw new NoSuchElementException();
											}
										} else {
											throw new NoSuchElementException();
										}
									} else if (tok.equalsIgnoreCase(",")) {
										// Continue to the next column
									} else if (tok.equalsIgnoreCase(")")) {
										done = true;
									} else {
										throw new NoSuchElementException();
									}
								} else {
									throw new DbmsError(
											"Invalid table column definition for "
													+ colName + ". '" + sql
													+ "'.");
								}
							} else {
								throw new NoSuchElementException();
							}

							// Everything is ok. Add the column to the table
							table.addColumn(new Column(colId, colName, colType,
									colLength, nullable));
							colId++;
						} else {
							// if(colId == 1) {
							throw new DbmsError(
									"Invalid table column identifier " + tok
											+ ". '" + sql + "'.");
							// }
						}
					}

					// Check for the semicolon
					tok = tokenizer.nextToken();
					if (!tok.equals(";")) {
						throw new NoSuchElementException();
					}

					// Check if there are more tokens
					if (tokenizer.hasMoreTokens()) {
						throw new NoSuchElementException();
					}

					if (table.getNumColumns() == 0) {
						throw new DbmsError(
								"No column descriptions specified. '" + sql
										+ "'.");
					}

					// The table is stored into memory when this program exists.
					tables.add(table);

					out.println("Table " + table.getTableName()
							+ " was created.");
				} else {
					throw new NoSuchElementException();
				}
			} else {
				throw new DbmsError("Invalid table identifier " + tok + ". '"
						+ sql + "'.");
			}
		} catch (NoSuchElementException ex) {
			throw new DbmsError("Invalid CREATE TABLE statement. '" + sql
					+ "'.");
		}
	}

	/**
	 * INSERT INTO
	 * <table name>
	 * VALUES ( val1 , val2, .... ) ;
	 * 
	 * @param sql
	 * @param tokenizer
	 * @throws Exception
	 */
	private void insertInto(String sql, StringTokenizer tokenizer)
			throws Exception {
		try {
			String tok = tokenizer.nextToken();
			if (tok.equalsIgnoreCase("INTO")) {
				tok = tokenizer.nextToken().trim().toUpperCase();
				Table table = null;
				for (Table tab : tables) {
					if (tab.getTableName().equals(tok)) {
						table = tab;
						break;
					}
				}

				if (table == null) {
					throw new DbmsError("Table " + tok + " does not exist.");
				}

				tok = tokenizer.nextToken();
				if (tok.equalsIgnoreCase("VALUES")) {
					tok = tokenizer.nextToken();
					if (tok.equalsIgnoreCase("(")) {
						tok = tokenizer.nextToken();
						String values = String.format("%3s", table.getData()
								.size() + 1)
								+ " ";
						int colId = 0;
						boolean done = false;
						while (!done) {
							if (tok.equals(")")) {
								done = true;
								break;
							} else if (tok.equals(",")) {
								// Continue to the next value
							} else {
								if (colId == table.getNumColumns()) {
									throw new DbmsError(
											"Invalid number of values were given.");
								}

								Column col = table.getColumns().get(colId);

								if (tok.equals("-") && !col.isColNullable()) {
									throw new DbmsError(
											"A NOT NULL column cannot have null. '"
													+ sql + "'.");
								}

								if (col.getColType() == Column.ColType.INT) {
									try {
										int temp = Integer.parseInt(tok);
									} catch (Exception ex) {
										throw new DbmsError(
												"An INT column cannot hold a CHAR. '"
														+ sql + "'.");
									}

									tok = String.format("%10s", tok.trim());
								} else if (col.getColType() == Column.ColType.CHAR) {
									int length = tok.length();
									if (length > col.getColLength()) {
										throw new DbmsError(
												"A CHAR column cannot exceede its length. '"
														+ sql + "'.");
									}

									tok = String.format(
											"%-" + col.getColLength() + "s",
											tok.trim());
								}

								values += tok + " ";
								colId++;
							}
							tok = tokenizer.nextToken().trim();
						}

						if (colId != table.getNumColumns()) {
							throw new DbmsError(
									"Invalid number of values were given.");
						}

						// Check for the semicolon
						tok = tokenizer.nextToken();
						if (!tok.equals(";")) {
							throw new NoSuchElementException();
						}

						// Check if there are more tokens
						if (tokenizer.hasMoreTokens()) {
							throw new NoSuchElementException();
						}

						// insert the value to table
						table.addData(values);
						out.println("One line was saved to the table. "
								+ table.getTableName() + ": " + values);
					} else {
						throw new NoSuchElementException();
					}
				} else {
					throw new NoSuchElementException();
				}
			} else {
				throw new NoSuchElementException();
			}
		} catch (NoSuchElementException ex) {
			throw new DbmsError("Invalid INSERT INTO statement. '" + sql + "'.");
		}
	}

	/**
	 * DROP TABLE
	 * <table name>
	 * ;
	 * 
	 * @param sql
	 * @param tokenizer
	 * @throws Exception
	 */
	private void dropTable(String sql, StringTokenizer tokenizer)
			throws Exception {
		try {
			// Get table name
			String tableName = tokenizer.nextToken();

			// Check for the semicolon
			String tok = tokenizer.nextToken();
			if (!tok.equals(";")) {
				throw new NoSuchElementException();
			}

			// Check if there are more tokens
			if (tokenizer.hasMoreTokens()) {
				throw new NoSuchElementException();
			}

			// Delete the table if everything is ok
			boolean dropped = false;
			for (Table table : tables) {
				if (table.getTableName().equalsIgnoreCase(tableName)) {
					table.delete = true;
					dropped = true;
					break;
				}
			}

			
			if (dropped) {
				out.println("Table " + tableName + " was dropped.");

			} else {
				out.println("Table " + tableName + " does not exist.");
			}
		} catch (NoSuchElementException ex) {
			throw new DbmsError("Invalid DROP TABLE statement. '" + sql + "'.");
		}

	}
	
	
	private  void loadplanTable(String from , String where)
	{
		int listcnt = 0;
		double tempFF = 0;
		int colpsoS = 0;
		int chkfl = 0;
		from = from.trim();
		where = where.trim();
		
		PlanTable pltab= new PlanTable();
		String[] query1 = where.split(" ");
		if(!from.isEmpty()){
			pltab.setQueryBlockNo(1);
		}else{
			pltab.setQueryBlockNo(0);
		}
		String[] query3	 = from.split(" ");
		
		for(String word: query3 )
			{
			 if(word.equalsIgnoreCase("ORDER"))
					 {
				 	   //System.out.println("Order found");
				 	   String tbN1 = from.substring(0,2);
				 	  // System.out.println("Table Name" + tbN1);
				 	   String gTbn = from.substring(12,14);
				 	 //  System.out.println("orderbyTable Name" + gTbn);
				 	   Integer gColn = Integer.parseInt(from.substring(16,17));
				 	//   System.out.println("orderbyColName" + gColn);
				 	  for (Table tabcs: tables )
						{
							if (tabcs.getTableName().equalsIgnoreCase(gTbn))
							{
								
										for(int i=0; i<tabcs.getIndexes().size();i++ )
										   {
											
										    for (int j=0; j<tabcs.getIndexes().get(i).getIdxKey().size();j++)
										     {
										    	
										      if( gColn.equals(tabcs.getIndexes().get(i).getIdxKey().get(j).colId) )
										      {
										    	     ATabName1.add(gTbn);
											    	 AColName1.add(tabcs.getIndexes().get(i).getIdxKey().get(j).colId);
											 		 AIndName1.add((i+1));
											 		 APPos1.add((j+1));
										    	  
										    	 String tmpSg = gTbn + '.' + 'X'+(i+1);
										    	// System.out.println("index to set " + tmpSg);
										    	 pltab.setAccessName(tmpSg); 
										    	 chkfl = 1;
										      }
										     }
										   }
							}
						}
				 	
				 	  
				 	 if (AColName1.size()==0)
				 	 {
				 		 pltab.setPrefetch('S');
				 		 pltab.setSortC_orderBy('Y');
				 	 }
				 	  
				 if (AColName1.size()!=0 && AColName1.size()!= 1 )
					 {
					 pltab.setIndexOnly('Y');
					int poschk1 = 0;
					
					int valL1 = 0;
					for(int y = 0; y< AColName1.size(); y++)
					{
						pltab.setAccessType('I'); 
						if(poschk1 == 0)
						 {
						  poschk1 = APPos1.get(y);
						 // String temps =  ATabName.get(y) +'.'+'X'+AIndName.get(y);
						//  pltab.setAccessName(temps);
						  
						  valL1 = y;	
						 } 
						else if(poschk1 > APPos1.get(y))
						{
							  poschk1 = APPos1.get(y);
							 // String temps =  ATabName.get(y) +'.'+'X'+AIndName.get(y);
							//  pltab.setAccessName(temps);
							  
							  valL1 = y;	
							
						}
											
						
					}
					String temps =  ATabName1.get(valL1) +'.'+'X'+AIndName1.get(valL1);
					pltab.setAccessName(temps);
					if(APPos1.get(valL1)==1)
					{
						pltab.matchCols = 1;
					}
					
					
					 }
					 else if( AColName1.size()== 1)
					 {
						 pltab.setAccessType('I'); 
						 String temps =  ATabName1.get(0) +'.'+'X'+AIndName1.get(0);
						 pltab.setAccessName(temps);
						 pltab.setIndexOnly('Y');
						 if(APPos1.get(0)==1)
						 {
							 pltab.matchCols = 1;
						 }else{ pltab.matchCols = 0;}
						 
					 }
					 			 	  
				 	  
				 	  
				 	  
				 	  
					 }
			}
		
		
		if(!where.equalsIgnoreCase("....."))
		{
			for(String word: query1)
		    {
				
				
		      if(word.length() == 5 && word.substring(2,3).equalsIgnoreCase( "." ))
		      {
		    	  
		    	  tlist.add( word.substring(0, 2));
		    	  tcollist.add(Integer.parseInt(word.substring(4, 5)));
		    	  listcnt++;
		      }
			}
			
		
		
		if (listcnt !=0)
		{
		for(int x = 0; x < listcnt; x = x+1)
		{
			for (Table tabc: tables )
			{
				if (tabc.getTableName().equalsIgnoreCase(tlist.get(x)))
				{
					
							for(int i=0; i<tabc.getIndexes().size();i++ )
							   {
								
							    for (int j=0; j<tabc.getIndexes().get(i).getIdxKey().size();j++)
							     {
							      if(tcollist.get(x).equals(tabc.getIndexes().get(i).getIdxKey().get(j).colId) )
							      {
							    	 ATabName.add(tlist.get(x));
							    	 AColName.add(tabc.getIndexes().get(i).getIdxKey().get(j).colId);
							 		 AIndName.add((i+1));
							 		 APPos.add((j+1));
							 		 colpsoS = tabc.getIndexes().get(i).getIdxKey().get(j).colId;
							 		 colpsoS --;
							 		// System.out.println(" Table Name" + tabc.getTableName() + " Col Name" +  tabc.getIndexes().get(i).getIdxKey().get(j).colId + " colCArd" + tabc.getColumns().get(colpsoS).getColCard()  );
							 		 tempFF = (double)1 /(double)tabc.getColumns().get(colpsoS).getColCard() ;
							 		 AFFilter.add(tempFF);
							    	// System.out.print("found column "  + tabc.getIndexes().get(i).getIdxKey().get(j).colId + "at index " + (i+1) + "at postion" + (j+1) ) ;
							       }
							      }
							    //System.out.println();
							   }
							}
					}
					
				
			
			// System.out.println(" List table and Col " + tlist.get(x) +tcollist.get(x) );
		
		  }
		}
		
		 if (AColName.size()==0)
	 	 {
	 		 pltab.setPrefetch('S');
	 		 
	 	 }
		
		 if (AColName.size()!=0 && AColName.size()!= 1 )
		 {
		//System.out.println("TableName   ColnName  IndexName  Position \n");
		int poschk = 0;
		double ffchk = 0;
		int valL = 0;
		for(int y = 0; y< AColName.size(); y++)
		{
			pltab.setAccessType('I'); 
			if(poschk == 0)
			 {
			  poschk = APPos.get(y);
			 // String temps =  ATabName.get(y) +'.'+'X'+AIndName.get(y);
			//  pltab.setAccessName(temps);
			  ffchk = AFFilter.get(y);
			  valL = y;	
			 } 
			else if(poschk > APPos.get(y))
			{
				  poschk = APPos.get(y);
				 // String temps =  ATabName.get(y) +'.'+'X'+AIndName.get(y);
				//  pltab.setAccessName(temps);
				  ffchk = AFFilter.get(y);
				  valL = y;	
				
			}else if(poschk == APPos.get(y))
			{
				if (ffchk > AFFilter.get(y))
				{
					  //String temps =  ATabName.get(y) +'.'+'X'+AIndName.get(y);
					 // pltab.setAccessName(temps);
					
					valL = y;
				}
				
				
			}
		
			
			
		}
		String temps =  ATabName.get(valL) +'.'+'X'+AIndName.get(valL);
		pltab.setAccessName(temps);
		if(APPos.get(valL)==1)
		{
			pltab.matchCols = 1;
		}
		
		
		 }
		 else if( AColName.size()== 1)
		 {
			 pltab.setAccessType('I'); 
			 String temps =  ATabName.get(0) +'.'+'X'+AIndName.get(0);
			 pltab.setAccessName(temps);
			 if(APPos.get(0)==1)
			 {
				 pltab.matchCols = 1;
			 }else{ pltab.matchCols = 0;}
			 
		 }
		 
		}
		
		int chsize = from.length();
		
		if (chsize == 2)
		{
			for(Table tab : tables )
			{
				if(from.contains(tab.getTableName())){
					int table1Card=tab.getTableCard();
					pltab.setTable1Card(table1Card);
				}
							
		    }
		
		}	
		else if(chsize == 7)
		{
			for(Table tab : tables){
				if(from.contains(tab.getTableName())){
					table1Card.add(tab.getTableCard());
					table2Card.add(tab.getTableName());
				}
			}
			
			int tab_card1 =  table1Card.get(0);
			pltab.setTable1Card(tab_card1);
			int tab_card2 =  table1Card.get(1);
			pltab.setTable2Card(tab_card2);
			if(tab_card1 < tab_card2){
				pltab.setLeadTable(table2Card.get(1));
				
			}else{
				pltab.setLeadTable(table2Card.get(0));
			}
		}else { 
			String tbN = from.substring(0,2);
			int flagC = 0;
			for(Table tab : tables )
			{
				if(tbN.equalsIgnoreCase(tab.getTableName())){
					int table1Card=tab.getTableCard();
					pltab.setTable1Card(table1Card);
					flagC = 1;
				}
						
		    }
			if (flagC == 0)
			{
				System.out.println("Table not Found Error:: " + tbN );
				System.exit(0);
				
			}
			
			
		}
		
		
		if(where.substring(0,1).equals("T") &&  ( where.substring(8,9).equals("T")) )
		{
			pltab.setPrefetch('S');
		}
		
		if(where.substring(0,1).equals("T") &&  ( where.substring(8,9).equals("T")) )
			{
			
			String temtab1 = where.substring(0,2);
			String temcoln1 = where.substring(3,5);
			//System.out.println("TableName1" + temtab1 + "ColName1" + temcoln1);
			String temtab2 = where.substring(8,10);
			String temcoln2 = where.substring(11,13);
			//System.out.println("TableName1" + temtab2 + "ColName1" + temcoln2);
			double tempFa1=0;
			for(Table tbn1: tables)
				{
					if (tbn1.getTableName().equalsIgnoreCase(temtab1) )
					{
						int csize1 = tbn1.getNumColumns();
						for (int z=0; z<csize1; z++)
						{
							if(tbn1.getColumns().get(z).getColName().equalsIgnoreCase(temcoln1))
									{
								     
								    tempFa1 = (double)1 /(double)tbn1.getColumns().get(z).getColCard() ;
								    //System.out.println("Filter FActor1" + tempFa1);
									}
						}
						
						
					}
				}
			double tempFa2=0;
			for(Table tbn2: tables)
			{
				if (tbn2.getTableName().equalsIgnoreCase(temtab2) )
				{
					int csize2 = tbn2.getNumColumns();
					for (int z=0; z<csize2; z++)
					{
						if(tbn2.getColumns().get(z).getColName().equalsIgnoreCase(temcoln2))
								{
							     
							    tempFa2 = (double)1 /(double)tbn2.getColumns().get(z).getColCard() ;
							   // System.out.println("Filter FActor2" + tempFa2);
								}
					}
					
					
				}
			}
			if(tempFa1 > tempFa2)
			{
				pltab.setLeadTable(temtab1);
			}else
			{
				pltab.setLeadTable(temtab2);
			}
			
			
		}
		
		pltab.printTable(out);	
		System.out.println("\n");
	}
	
	
	private  void loadpredicateTable(String Where)
	{

		int Rflag = 0;
		String tempwhere = Where.trim();
		for (int x=0 ; x<2 ; x++ )
		{
		 
			
		}
			
				
		int i=0,j=0;
		int pos=-2;	
		boolean OR_flag =false,
				AND_flag=false , IN_flag=false;
		ArrayList<String> pred_array = new ArrayList<String>();
        String[][] P_Table = new String[6][8]; 
                                                                  
        for(j=0;j<8;j++)  P_Table[0][j]="Pred_Tab Type C1 C2 FF1 FF2 Seq Text".split(" ")[j];
        for(j=1;j<6;j++)  P_Table[j][0]="PredNo "+j; 
        for(i=1;i<6;i++)
        	for(j=1;j<8;j++)
        		 P_Table[i][j]="--";
        
    	Where = Where.trim();
		String[] query = Where.split(" ");
		
																 
		for(String word: query)
		    {
		      if(word.equalsIgnoreCase("and"))
		         AND_flag =true;
		      if(word.equalsIgnoreCase("or"))
		         OR_flag  =true;
		      if(word.equalsIgnoreCase("in"))
			         IN_flag = true;
		   	}
		

if(IN_flag)
{
	

}
		
		
		
if(query.length>1)
{
	  if(AND_flag)
		    for(i=0;i<query.length;i+=4)
				   pred_array.add(query[i] + query[i+1] + query[i+2]);
			
		
	   else if(OR_flag)
    	   {   
              flag_In_list = true;
              

			  String[] query_or = Where.split(" OR ");
          
			  for(i=0;i<query_or.length;i++)
		         { 
				    pos = search(query_or[i],pred_array);
		         
		    	    if(pos!=-1)
		    	        pred_array.set(pos,pred_array.get(pos) + " OR " + query_or[i]); 
		       	    else
		               pred_array.add(query_or[i]);
		         }
			  for(i=0;i<pred_array.size();i++)
				  pred_array.set(i, pred_array.get(i).replaceAll(" ", ""));
			  for(i=0;i<pred_array.size();i++)
				  pred_array.set(i, pred_array.get(i).replaceAll("OR", " OR "));
		     }  
	       
	     else
	    	 for(i=0;i<query.length;i+=4)
			   pred_array.add(query[i] + query[i+1] + query[i+2]);
	  
	

}   
   for(i=0;i<pred_array.size();i++)                                   
   {	 P_Table[i+1][7] = pred_array.get(i);     
     
   
         for(String word: pred_array.get(i).split(""))
		    {
        	
		      if(word.equalsIgnoreCase("="))
        	    P_Table[i+1][1] = "E";
		      else if(word.equalsIgnoreCase(">"))
		      {
		       	P_Table[i+1][1] = "R";  
		        Rflag = 1;
		      }
		       	else if(word.equalsIgnoreCase("<"))
		       	{
		    	  P_Table[i+1][1] = "R"; 
		    	  Rflag = 1;
		       	}
		    }
   }
                                                                    
    
   
   
   for(i=0;i<pred_array.size();i++)
   {
	   
	   String table_name = P_Table[i+1][7].substring(0, 2);
	   double FF=0;
	   int card=0;
	  
	   int    col_name   = Integer.parseInt(P_Table[i+1][7].substring(4,5));
	  
	  
		for (Table tabName : tables) 
	     {
		   if(table_name.equals(tabName.getTableName() ))
		   {
			   
			    int l=0;
			    while(l<tabName.getColumns().size())
			    {
			    	if(col_name == tabName.getColumns().get(l).getColId() )
			    	{	
			    		card =tabName.getColumns().get(l).getColCard() ;     					
			    		P_Table[i+1][2] =Integer.toString(card);
			    		
			    		String[] tempstr = P_Table[i+1][7].split(" OR ");	
			    	
			    		FF = (double)tempstr.length/(double)card; 
			    		
			    		P_Table[i+1][4] = Double.toString(FF);
			    		if(P_Table[i+1][4].length()>5)
			    			P_Table[i+1][4]=P_Table[i+1][4].substring(0, 6);
			    		
			    	}
			    l++;
			    }

		   }
	     }
	   
	   
   
   
   }
                      
   P_Table = predicate_sequence(P_Table,pred_array);
   
   
   
   				HashSet noDupSet = new HashSet();
   					for (int k = 0; k < pred_array.size(); k++)
       					noDupSet.add(pred_array.get(k).substring(0, 4));
     			num_of_distintict_cols_in_where=noDupSet.size();
 
     			if(tempwhere.substring(0,1).equals("T") &&  ( tempwhere.substring(8,9).equals("T")) )
     			{
     				String temtab = tempwhere.substring(8,10);
     				String temcoln = tempwhere.substring(11,13);
     				//System.out.println("TableName ::" + temtab + " ColnName:: " + temcoln);
     				for(Table tbn: tables)
     				{
     					if (tbn.getTableName().equalsIgnoreCase(temtab) )
     					{
     						int csize = tbn.getNumColumns();
     						for (int z=0; z<csize; z++)
     						{
     							if(tbn.getColumns().get(z).getColName().equalsIgnoreCase(temcoln))
     									{
     								    double tempFa=0; 
     								    tempFa = (double)1 /(double)tbn.getColumns().get(z).getColCard() ;
     								    P_Table[1][3]= String.valueOf( tbn.getColumns().get(z).getColCard()) ;
     								    P_Table[1][5]= String.valueOf(tempFa);
     									}
     						}
     						
     						
     					}
     				}
     				
     			}
     			
     			if(AND_flag)
     			{
     				//System.out.println("1st predicate " + tempwhere.substring(0,5) + "2nd predicate " + tempwhere.substring(15,20) );
     				if(tempwhere.substring(0,5).equals(tempwhere.substring(15,20)) )
     				{
     					 P_Table[1][6]= String.valueOf(0);
						 P_Table[2][6]= String.valueOf(0);
     				}
     				
     			}
     			
     			if(OR_flag && Rflag == 1)
     			{
     				//System.out.println("1st predicate " + tempwhere.substring(0,7) + "2nd predicate " + tempwhere.substring(14,21) );
     				if(tempwhere.substring(0,7).equals(tempwhere.substring(14,21)) )
     				{
     					 P_Table[1][7]= tempwhere.substring(0,11);
						 P_Table[2][7]= tempwhere.substring(14,24);
						 P_Table[2][1]= P_Table[1][1];
						 P_Table[2][2]= P_Table[1][2];
						 P_Table[2][4]= P_Table[1][4];
						 P_Table[2][6]= String.valueOf(0);
     				}
     				
     			}
     			
     			
     			System.out
				.println("-------------------------------------------------------------------------------------------");
		System.out.printf("%-20s %-7s %-7s %-7s %-7s %-7s %-7s %-25s %-25s %n",
				"| Predicate Table", "| Type", "| C1", "| C2", "| FF1",
				"| FF2", "| Seq", "| Text", "| Description");
		System.out
				.println("-------------------------------------------------------------------------------------------");
			for(int i1=1;i1<6;i1++)
	    		{System.out.println();
	    		System.out.print("         ");
	    		
	    		for(int j1=0;j1<8;j1++)
	    			 
	    			System.out.print( P_Table[i1][j1]+ "      ");
	    		}
		    	   
   }
	
	
	private String[][] predicate_sequence(String[][] P_Table, ArrayList<String> pred_array)
	{
		
		ArrayList<Double> temp_arr = new ArrayList<>();
		
		  for(int i=0;i<pred_array.size();i++)
		     temp_arr.add(Double.parseDouble(P_Table[i+1][4])); 
		  
		  Collections.sort(temp_arr);
		  
		  for(int i=0;i<pred_array.size();i++)
		  {
			  for(int j=0;j<pred_array.size();j++)
			  {
				  if(temp_arr.get(i) == Double.parseDouble(P_Table[j+1][4] ))
						 { 
					        if(P_Table[j+1][6].equals("--"))
					         {
					        	P_Table[j+1][6] = Integer.toString(i+1);
					            break;
					         }  
						  }
			  }
		  }
		  
		
		return P_Table;
	}
	
	private void selectsqlscan(String ref_sql)
	{
		
		DBMS db1 = new DBMS();
		  boolean where_flag = false ,orderby_flag =false;
			String Select  = "....."  ,
			       From    = "....."  ,
			       Where   = "....."  ,
			       Orderby = "....."  ;
			       
		
			String orig_sentence = ref_sql;
		
			String sentence = orig_sentence.trim();
			
			String[] query1 = sentence.split(" ");
			
			
			for(String word: query1)
			    {
					
			      if(word.equalsIgnoreCase("where"))
			          { where_flag   =true; flag_query_has_where = true; }
			      if(word.equalsIgnoreCase("orderby"))
			         { orderby_flag =true;  flag_query_has_orderby =true;}
			    
		     	}
			
			
			Pattern pattern = Pattern.compile("(?<=SELECT).*.(?=FROM)");
			Matcher matcher = pattern.matcher(sentence);
	
			        while (matcher.find()) 
			            Select = matcher.group().toString();
			        	
			            		
			        
			if(where_flag && orderby_flag)
			{
	 		   pattern = Pattern.compile("(?<=FROM).*.(?=WHERE)");
			   matcher = pattern.matcher(sentence);
	
					        while (matcher.find()) 
					        	From= matcher.group().toString();
			  
	                               
	           pattern = Pattern.compile("(?<=WHERE).*.(?=ORDERBY)");
			   matcher = pattern.matcher(sentence);
	
			                while (matcher.find())
			                     Where= matcher.group().toString();
			   
			                
	           pattern = Pattern.compile("(?<=ORDERBY).*.(?=;)");
	  		   matcher = pattern.matcher(sentence);
	
	  		                while (matcher.find()) 
	  		                	Orderby= matcher.group().toString();
			   
					                		        
			
			}
			else if(where_flag && !orderby_flag)
			{
		 		   pattern = Pattern.compile("(?<=FROM).*.(?=WHERE)");
				   matcher = pattern.matcher(sentence);
	
						        while (matcher.find()) 
						        	From= matcher.group().toString();
				   
		           
		           pattern = Pattern.compile("(?<=WHERE).*.(?=;)");
				   matcher = pattern.matcher(sentence);
	
				                while (matcher.find()) 
				                	 Where= matcher.group().toString();
				   
				 }
			else if(!where_flag && orderby_flag)
			{
			
		 		   pattern = Pattern.compile("(?<=FROM).*.(?=ORDERBY)");
				   matcher = pattern.matcher(sentence);
	
						        while (matcher.find()) 
						        	From= matcher.group().toString();
	     		   
		           
		           pattern = Pattern.compile("(?<=ORDER).*.(?=;)");
				   matcher = pattern.matcher(sentence);
	
				                while (matcher.find()) 
				                	Orderby= matcher.group().toString();
	     		   
				 }
			else
			   {
		   		   pattern = Pattern.compile("(?<=FROM).*.(?=;)");
				   matcher = pattern.matcher(sentence);
	
						        while (matcher.find()) 
						        	From= matcher.group().toString();
	     		   
			   }
			
			
	    	try {
				db1.loadTables();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    	db1.loadplanTable(From,Where);
	        db1.loadpredicateTable(Where);
	    	
	   
	}

	
	public int search(String PredX,ArrayList<String> pred_arrayX)
	{
		 for (int j=0; j<pred_arrayX.size(); j++) 
		 {
			 //System.out.println(PredX.substring(0,PredX.indexOf(" ")) + ":"+equals(pred_arrayX.get(j).substring(0,pred_arrayX.get(j).indexOf(" "))));
			 if(PredX.substring(0,PredX.indexOf(" ")).equals(pred_arrayX.get(j).substring(0,pred_arrayX.get(j).indexOf(" "))))
			      return j;
		 }
      return -1;
	}
	


	void tableIndex(String tableName)
	{
		
		for (Table table1 : tables)
		{
			if(table1.getTableName().equalsIgnoreCase(tableName))
			{
				for(int i=0; i<table1.getIndexes().size();i++ )
				   {
					
				    for (int j=0; j<table1.getIndexes().get(i).getIdxKey().size();j++)
				     {
				      System.out.print( table1.getIndexes().get(i).getIdxKey().get(j).colId ) ;
				     }
				    System.out.println();
				   }
				}
		}
		
		for (Table table : tables)
		{
			if(table.getTableName().equalsIgnoreCase(tableName))
			{
				
				
				for (Index Ind: table.getIndexes()){
					
				System.out.println("Tables Indexes are::" + Ind.getIdxName()  ) ;
					int i = 0;
					int maxCols = Ind.getIdxKey().size();
					for (int j = 0; j < maxCols; j++) {
						if (i < Ind.getIdxKey().size()) {
							Index.IndexKeyDef def = Ind.getIdxKey().get(i);
							if (def.descOrder) {
								String ad;
								
								System.out.println(def.colId + "D");
							} else {
								System.out.println(def.colId + "A");
							}

						   } 
						i++;
						}
						
					}

				
					
					
				}
				
			
			}
		}
		
	
	private void storeTableFile(Table table) throws FileNotFoundException {
		File tableFile = new File(TABLE_FOLDER_NAME, table.getTableName()
				+ TABLE_FILE_EXT);

		// Delete the file if it was marked for deletion
		if (table.delete) {
			try {
				tableFile.delete();
			} catch (Exception ex) {
				out.println("Unable to delete table file for "
						+ table.getTableName() + ".");
			}
		} else {
			// Create the table file writer
			PrintWriter out = new PrintWriter(tableFile);

			// Write the column descriptors
			out.println(table.getNumColumns());
			for (Column col : table.getColumns()) {
				if (col.getColType() == Column.ColType.INT) {
					out.println(col.getColName() + " I " + col.isColNullable());
				} else if (col.getColType() == Column.ColType.CHAR) {
					out.println(col.getColName() + " C" + col.getColLength()
							+ " " + col.isColNullable());
				}
			}

			// Write the index info
			out.println(table.getNumIndexes());

			for (Index index : table.getIndexes()) {

				if (!index.delete) {
					String idxInfo = index.getIdxName() + " "
							+ index.getIsUnique() + " ";

					for (Index.IndexKeyDef def : index.getIdxKey()) {
						idxInfo += def.colId;
						if (def.descOrder) {
							idxInfo += "D ";
						} else {
							idxInfo += "A ";
						}
					}
					out.println(idxInfo);
				}
			}

			// Write the rows of data
			out.println(table.getData().size());
			for (String data : table.getData()) {
				out.println(data);
			}

			// Write RUNSTATS
						out.println("STATS TABCARD " + table.getTableCard());
						for (int i = 0; i < table.getColumns().size(); i++) {
							Column col = table.getColumns().get(i);
							if(col.getHiKey() == null)
								col.setHiKey("-");
							if(col.getLoKey() == null)
								col.setLoKey("-");
							out.println("STATS COLCARD " + i + " " + col.getColCard() + " " + col.getHiKey() + " " + col.getLoKey());
						}
			
			
			
			
			out.flush();
			out.close();
		}

		// Save indexes to file
		for (Index index : table.getIndexes()) {

			File indexFile = new File(TABLE_FOLDER_NAME, table.getTableName()
					+ index.getIdxName() + INDEX_FILE_EXT);
			
			// Delete the file if it was marked for deletion
			if (index.delete) {
				try {
					indexFile.delete();
				} catch (Exception ex) {
					out.println("Unable to delete index file for "
							+ indexFile.getName() + ".");
				}
			} else {
				PrintWriter out = new PrintWriter(indexFile);
				String idxInfo = index.getIdxName() + " " + index.getIsUnique()
						+ " ";

				// Write index definition
				for (Index.IndexKeyDef def : index.getIdxKey()) {
					idxInfo += def.colId;
					if (def.descOrder) {
						idxInfo += "D ";
					} else {
						idxInfo += "A ";
					}
				}
				out.println(idxInfo);

				// Write index keys
				out.println(index.getKeys().size());
				for (Index.IndexKeyVal key : index.getKeys()) {
					String rid = String.format("%3s", key.rid);
					out.println(rid + " '" + key.value + "'");
					// out.println(key.rid + " '" + key.value + "'");
				}

				out.flush();
				out.close();

			}
		}

	}


	public boolean checkIfIndexExists(Table table, Index idx) {
		boolean r = false;
		for (Index i : table.getIndexes()) {
			if (i.getIdxName().equals(idx.getIdxName()))
				r = true;
		}
		return r;
	}

	private void DropTableIndex(String sql, StringTokenizer tokenizer)
			throws Exception {
		try {
			// Get table name
			String indexName = tokenizer.nextToken();

			String tok = tokenizer.nextToken();
			if (!tok.equals(";")) {
				throw new NoSuchElementException();
			}

			// Check if there are more tokens
			if (tokenizer.hasMoreTokens()) {
				throw new NoSuchElementException();
			}

			// Delete the table if everything is ok
			boolean dropped = false;
			for (Table table : tables) {
				for (Index idx : table.getIndexes()) {
					if (idx.getIdxName().equalsIgnoreCase(indexName)) {
						idx.delete = true;
						dropped = true;
						table.setNumIndexes(table.getNumIndexes() - 1);
						break;
					}
				}
			}

			if (dropped) {
				out.println("Index " + indexName + " is dropped from the table");
			} else {
				out.println("Index " + indexName + " is not Present check the Index Name.");

			}
		} catch (NoSuchElementException ex) {
			throw new DbmsError("Invalid DROP INDEX statement. '" + sql + "'.");
		}

	}


}
