package data;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.ArrayList;

import javax.imageio.ImageIO;




/**
 * Classe che permette di fare le operazioni nel database relative alle pagine
 * @author Luca
 *
 */

public class PageDAO implements PagesDAO {

	/**
	 * Metodo che premette di prendere le pagine relative a un opera dal database. Se sono presenti questo 
	 * ritorna un OperaComp altrimenti un OperaGen
	 * @param opera titolo di cui prendere le pagine
	 * @return OperaGen oppure OperaComp
	 */
	public OperaGen selectPages(String opera){
		
		Connection conn= dbConnect.connect();
		PreparedStatement pstmt;
		ResultSet rs;
		
		OperaGen a= null;
		int count=0;	
		try {
			String sql="SELECT img, acquisitore, revisoreimg, trascrittore, revisoretei, numpag, imgpubb, teipubb, testo  from pagina LEFT JOIN tei ON pagina.id=tei.idpagina WHERE pagina.titoloopera=? ORDER BY numpag ASC";
		    pstmt =conn.prepareStatement(sql);
		    pstmt.setString(1, opera);
		    pstmt.execute();
		    rs = pstmt.getResultSet();
		    if(rs.isBeforeFirst())  {      
			        ArrayList<Page> pagine= new ArrayList<Page>();
			        a= new OperaComp(null, null, null, null, 0, pagine);
			        while(rs.next()){
			        	++count;
			        	Trascrizione tras= new Trascrizione(rs.getString("testo"), rs.getString("trascrittore"), rs.getString("revisoretei"), rs.getBoolean("teipubb"));
			        	Blob blob = rs.getBlob("img");
		                int blobLength = (int) blob.length();
		                byte[] blobAsBytes = blob.getBytes(1, blobLength);
		                BufferedImage bufferedImage;
						try {
							bufferedImage = ImageIO.read(new ByteArrayInputStream(blobAsBytes));
							Immagine img =new Immagine(bufferedImage, rs.getString("acquisitore"), rs.getString("revisoreimg"), rs.getBoolean("imgpubb"));
							Page k= new Page(count, img, tras);
				        	pagine.add(k);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}			  			        	
			        }
			        ((OperaComp)a).setPagTot(count);
			        ((OperaComp)a).setPagine(pagine);
			        pstmt=conn.prepareStatement("SELECT * FROM opera WHERE titolo=?");
			        pstmt.setString(1, opera);
					if (pstmt.execute()){
						rs = pstmt.getResultSet(); 
						while(rs.next()){
							a.setAutore(rs.getString("autore"));
						    a.setEpoca(rs.getString("epoca"));
						    a.setNomeOpera(rs.getString("titolo"));
						    a.setPubblicata(rs.getBoolean("pubblicato"));
						    }    
					}
					rs.close();
					pstmt.close();
					conn.close();
					return a;
		    }
		    else
		    {
		    	  
		    	a=new OperaGen(null, null, null, null);
		    	pstmt.close();
		    	rs.close();
		    	pstmt=conn.prepareStatement("SELECT * FROM opera WHERE titolo=?");
		        pstmt.setString(1, opera);
		    	 if (pstmt.execute()){
					      rs = pstmt.getResultSet(); 
					      while(rs.next()){
					        	a.setAutore(rs.getString("autore"));
					        	a.setEpoca(rs.getString("epoca"));
					        	a.setNomeOpera(rs.getString("titolo"));
					        	a.setPubblicata(rs.getBoolean("pubblicato"));
					        	
					        }
		    		  }
		    		  rs.close();
		    		  pstmt.close();
		    		  conn.close();
		    		  return a;
		    }
		}
		catch (SQLException ex){
		    
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		return a;
	}

}
