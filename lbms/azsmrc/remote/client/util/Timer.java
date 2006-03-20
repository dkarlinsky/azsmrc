/*
 * File    : Timer.java
 * Created : 21-Nov-2003
 * By      : parg
 *
 * Azureus - a Java Bittorrent client
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package lbms.azsmrc.remote.client.util;

/**
 * @author parg
 *
 */

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Timer
	implements Runnable
{
	protected ThreadPoolExecutor	thread_pool;

	protected Set<TimerEvent>	events = new TreeSet<TimerEvent>();

	protected long	unique_id_next	= 0;

	protected volatile boolean	destroyed;

	public
	Timer(
		String	name )
	{
		this( name, 1 );
	}

	public
	Timer(
		String	name,
		int		thread_pool_size )
	{
		thread_pool = new ThreadPoolExecutor(thread_pool_size,50,1,TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>(), new TimerDeamonThreadFactory());

		Thread t = new Thread(this, "Timer:" + name );

		t.setDaemon( true );

		t.start();
	}

	public void
	run()
	{
		try{
			runSupport();

		}catch( Throwable e ){

			e.printStackTrace();
		}
	}

	public void
	runSupport()
	{
		while( true ){

			try{
				List<TimerEvent>	events_to_run = new ArrayList<TimerEvent>();

				synchronized(this){

					if ( destroyed ){

						break;
					}

					if ( events.isEmpty()){

						// System.out.println( "waiting forever" );

						this.wait();

					}else{
						long	now = System.currentTimeMillis();

						TimerEvent	next_event = events.iterator().next();

						long	delay = next_event.getWhen() - now;

						if ( delay > 0 ){

							// System.out.println( "waiting for " + delay );

							this.wait(delay);
						}
					}

					if ( destroyed ){

						break;
					}

					long	now = System.currentTimeMillis();

					Iterator<TimerEvent>	it = events.iterator();

					while( it.hasNext()){

						TimerEvent	event = it.next();

						if ( event.getWhen() <= now ){

							events_to_run.add( event );

							it.remove();
						}
					}
				}

				for (int i=0;i<events_to_run.size();i++){

					// System.out.println( "firing event");

					TimerEvent	ev = events_to_run.get(i);

					ev.setHasRun();

					thread_pool.execute(ev.getRunnable());
				}

			}catch( Throwable e ){

				e.printStackTrace(  );
			}
		}
	}

	/**
	 * Adds a Event, use System.currentTimeMillis()+ <i>yourTime</i>
	 * to add the Event.
	 *
	 * @param when timestamp
	 * @param performer
	 * @return
	 */
	public synchronized TimerEvent
	addEvent(
		long				when,
		TimerEventPerformer	performer )
	{
		return( addEvent( System.currentTimeMillis(), when, performer ));
	}

	public synchronized TimerEvent
	addEvent(
		long				creation_time,
		long				when,
		TimerEventPerformer	performer )
	{
		TimerEvent	event = new TimerEvent( this, unique_id_next++, creation_time, when, performer );

		events.add( event );

		// System.out.println( "event added (" + when + ") - queue = " + events.size());

		notify();

		return( event );
	}

	public synchronized TimerEventPeriodic
	addPeriodicEvent(
		long				frequency,
		TimerEventPerformer	performer )
	{
		TimerEventPeriodic periodic_performer = new TimerEventPeriodic( this, frequency, performer );

		return( periodic_performer );
	}

	protected synchronized void
	cancelEvent(
		TimerEvent	event )
	{
		if ( events.contains( event )){

			events.remove( event );

			// System.out.println( "event cancelled (" + event.getWhen() + ") - queue = " + events.size());

			notify();
		}
	}

	public synchronized void
	destroy()
	{
		destroyed	= true;
		//thread_pool.shutdown();
		notify();
	}

	public synchronized void
	dump()
	{
		System.out.println( "Timer '" + "': dump" );

		Iterator<TimerEvent>	it = events.iterator();

		while(it.hasNext()){

			TimerEvent	ev = it.next();

			System.out.println( "\t" + ev + ": when = " + ev.getWhen() + ", run = " + ev.hasRun() + ", can = " + ev.isCancelled());
		}
	}
}
