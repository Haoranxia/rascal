package org.rascalmpl.interpreter.cursors;

import org.rascalmpl.library.util.Cursor;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.IMap;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;

public class MapContext extends Context {
	// todo: pull up ctx
	private final Context ctx;
	private final IValue key;
	private final IMap map;

	public MapContext(Context ctx, IValue key, IMap map) {
		this.ctx = ctx;
		this.key = key;
		this.map = map;
	}

	@Override
	public IValue up(IValue focus) {
		return new MapCursor(map.put(key, focus), ctx);
	}

	@Override
	public IList toPath(IValueFactory vf) {
		return ctx.toPath(vf).append(vf.constructor(Cursor.Nav_lookup, key));
	}

}
