package com.how2java;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

import com.util.LuceneUtils;

public class LuceneWithUtil {
	public static void main(String[] args) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 1. 索引
		System.out.println("开始创建索引:"+dateFormat.format(new Date()));
		createIndex();
		System.out.println("索引创建完成:"+dateFormat.format(new Date()));
		// 2. 查询器
		
        Scanner s = new Scanner(System.in);
        
        while(true){
        	Analyzer analyzer = LuceneUtils.getAnalyzer();
        	System.out.print("请输入查询关键字：");
            String keyword = s.nextLine();
            System.out.println("当前关键字是："+keyword);
    		Query query = new QueryParser( "name",  analyzer).parse(keyword);

    		// 3. 搜索
    		IndexSearcher searcher = LuceneUtils.getIndexSearcher();
    		int numberPerPage = 10;
    		ScoreDoc[] hits = searcher.search(query, numberPerPage).scoreDocs;
    		
    		// 5. 显示查询结果
    		showSearchResults(searcher, hits,query,analyzer);
        }
		
		
		
		

	}

	private static void showSearchResults(IndexSearcher searcher, ScoreDoc[] hits, Query query,Analyzer analyzer) throws Exception {
		System.out.println("找到 " + hits.length + " 个命中.");

        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<span style='color:red'>", "</span>");
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, new QueryScorer(query));

        
        System.out.println("找到 " + hits.length + " 个命中.");
        System.out.println("序号\t匹配度得分\t结果");
		for (int i = 0; i < hits.length; ++i) {
			ScoreDoc scoreDoc= hits[i];
			int docId = scoreDoc.doc;
			Document d = searcher.doc(docId);
			List<IndexableField> fields= d.getFields();
			System.out.print((i + 1) );
			System.out.print("\t" + scoreDoc.score);
			for (IndexableField f : fields) {

				
				
				if("name".equals(f.name())){
		            TokenStream tokenStream = analyzer.tokenStream(f.name(), new StringReader(d.get(f.name())));
		            String fieldContent = highlighter.getBestFragment(tokenStream, d.get(f.name()));
					System.out.print("\t"+fieldContent);
				}
				else{
					System.out.print("\t"+d.get(f.name()));
				}
			}
			System.out.println("<br>");
		}
	}

	private static void createIndex() throws IOException {
		IndexWriter writer = LuceneUtils.getIndexWriter();
		String fileName = "140k_products.txt";
		List<Product> products = ProductUtil.file2list(fileName);
		int total = products.size();
		int count = 0;
		int per = 0;
		int oldPer =0;
		for (Product p : products) {
			addDoc(writer, p);
			/*count++;
			per = count*100/total;
			if(per!=oldPer){
				oldPer = per;
				System.out.printf("索引中，总共要添加 %d 条记录，当前添加进度是： %d%% %n",total,per);
			}*/
			
		}
		writer.close();
	}

	private static void addDoc(IndexWriter w, Product p) throws IOException {
		Document doc = new Document();
		doc.add(new TextField("id", String.valueOf(p.getId()), Field.Store.YES));
		doc.add(new TextField("name", p.getName(), Field.Store.YES));
		doc.add(new TextField("category", p.getCategory(), Field.Store.YES));
		doc.add(new TextField("price", String.valueOf(p.getPrice()), Field.Store.YES));
		doc.add(new TextField("place", p.getPlace(), Field.Store.YES));
		doc.add(new TextField("code", p.getCode(), Field.Store.YES));
		w.addDocument(doc);
	}
}
