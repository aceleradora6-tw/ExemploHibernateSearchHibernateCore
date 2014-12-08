package dao;

import java.util.List;

import model.Empresa;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.SessionScoped;

@Component
@SessionScoped
public class PseudoDao {
	
	private Session session;
	private static SessionFactory factory;
	
	public PseudoDao(){
		
		if(factory == null){
			Configuration config = new Configuration();
			config.configure();
			factory = config.buildSessionFactory();				
		}
		
		session = factory.openSession();		
	}

	public void inserir() {
		Empresa e = new Empresa();
		e.setCnpj("1234748");
		e.setNomeFantasia("Persistencia via JPA.");
		e.setRazaoSocial("Persistência via JPA.");
		
		if(!session.isOpen()){
			session = factory.openSession();
		}
		
		Transaction tx = session.getTransaction();
		tx.begin();
		session.save(e);
		tx.commit();
		
		System.out.println("Empresa inserida.");
		System.out.println("Empresa ID: " + e.getId());

		session.close();
	}
	
	public void indexarDados(){
		
		if(!session.isOpen()){
			session = factory.openSession();
		}
		
		FullTextSession fullTextSession = Search.getFullTextSession(session);
		try{
			fullTextSession.createIndexer().startAndWait();
		}catch(InterruptedException ex){
			System.out.println("Erro ao indexar: "+ ex.getMessage());
		}
		
		session.close();
		
		System.out.println("Indexação completada.");
	}
	
	public List<Empresa> buscar(String busca){
		if(!session.isOpen()){
			session = factory.openSession();
		}
		
		FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(session);
		Transaction tx = session.getTransaction();
		tx.begin();
		
		QueryBuilder qb = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Empresa.class).get();
		org.apache.lucene.search.Query luceneQuery = qb
				.keyword()
				.onField("nomeFantasia")
				.matching(busca)
				.createQuery();
		org.hibernate.Query hibernateQuery = fullTextSession.createFullTextQuery(luceneQuery, Empresa.class);
		
		List<Empresa> resultadoBusca = hibernateQuery.list();
				
		tx.commit();
		session.close();
		
		return resultadoBusca;
	}
}
