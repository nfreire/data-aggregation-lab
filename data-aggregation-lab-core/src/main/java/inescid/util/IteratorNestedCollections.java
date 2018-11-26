package inescid.util;

import java.io.File;
import java.util.Iterator;

//TODO
public class IteratorNestedCollections<A> implements Iterable<A> {

	@Override
	public Iterator<A> iterator() {
		return  new Iterator<A>() {
			A next=null;
			int idx1=-1;
			int idx2=0;
			File[] col1;
			File[] col2;
			{
				File datasetFolder = new File("TODO");
				if (datasetFolder.exists()) {
					col1 = datasetFolder.listFiles();
					prepareNext();
				}
			}
			@Override
			public boolean hasNext() {
				return next!=null;
			}
			
			@Override
			public A next() {
				A ret=next;
				prepareNext();
				return ret;
			}

			private void prepareNext() {
				next=null;
				OUT: while(next==null && idx1<col1.length-1) {
					idx1++;
					idx2=0;
					while(true) {
						if(!col1[idx1].isDirectory() || col1[idx1].getName().equals("seealso"))
							idx1++;
						else
							break;
						if(idx1>=col1.length)
							break OUT;
					}
					col2=col1[idx1].listFiles();
					while(idx2<=col2.length) {
						if(!col2[idx2].isFile())
							continue;
						if(!col2[idx2].getName().endsWith(".meta.xml")) {
							next=(A) col2[idx2];
							break OUT;
						}
					}
				}
			}
	};
	}
}
