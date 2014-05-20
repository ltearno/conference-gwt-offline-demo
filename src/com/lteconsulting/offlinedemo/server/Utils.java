package com.lteconsulting.offlinedemo.server;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/*
 * Server utility class.
 *  - Retrieving an EntityManager
 *  - Securing a Java call into a DB transaction
 */
public class Utils
{
	private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory( "offlinedemo" );

	public interface Transaction<T>
	{
		T execute( EntityManager entityManager );
	}

	public static <T> T executeTransaction( Transaction<T> transaction )
	{
		T res = null;

		EntityManager em = createEntityManager();

		em.getTransaction().begin();

		try
		{
			res = transaction.execute( em );

			em.getTransaction().commit();
		}
		catch(Exception e )
		{
			em.getTransaction().rollback();
			throw e;
		}
		finally
		{
			em.close();
		}

		return res;
	}

	public static EntityManager createEntityManager()
	{
		return emf.createEntityManager();
	}
}
