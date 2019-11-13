package org.rascalmpl.interpreter.cursors;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.rascalmpl.values.uptr.IRascalValueFactory;

import io.usethesource.vallang.IMap;
import io.usethesource.vallang.IMapWriter;
import io.usethesource.vallang.IRelation;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.type.Type;

public class MapCursor extends Cursor implements IMap {

	public MapCursor(IMap value) {
		super(value);
	}
	
	public MapCursor(IMap value, Context ctx) {
		super(value, ctx);
	}
	
	@Override
	public IMapWriter writer() {
	    return IRascalValueFactory.getInstance().mapWriter();
	}
	
	@Override
	public IRelation<IMap> asRelation() {
	    throw new UnsupportedOperationException();
	}
	 
	private IMap getMap() {
		return (IMap)getWrappedValue();
	}

	@Override
	public boolean isEmpty() {
		return getMap().isEmpty();
	}

	@Override
	public int size() {
		return getMap().size();
	}

	@Override
	public IMap put(IValue key, IValue value) {
		return new MapCursor(getMap().put(key,  value), getCtx());
	}
	
	@Override
	public IMap removeKey(IValue arg0) {
		return new MapCursor(getMap().removeKey(arg0), getCtx());
	}

	@Override
	public IValue get(IValue key) {
		Context ctx = new MapContext(getCtx(), key, getMap());
		return CursorFactory.makeCursor(getMap().get(key), ctx);
	}

	@Override
	public boolean containsKey(IValue key) {
		return getMap().containsKey(key);
	}

	@Override
	public boolean containsValue(IValue value) {
		return getMap().containsValue(value);
	}

	@Override
	public Type getKeyType() {
		return getMap().getKeyType();
	}

	@Override
	public Type getValueType() {
		return getMap().getValueType();
	}

	@Override
	public IMap join(IMap other) {
		return new MapCursor(getMap().join(other), getCtx());
	}

	@Override
	public IMap remove(IMap other) {
		return new MapCursor(getMap().remove(other), getCtx());
	}

	@Override
	public IMap compose(IMap other) {
		return new MapCursor(getMap().compose(other), getCtx());
	}

	@Override
	public IMap common(IMap other) {
		return new MapCursor(getMap().common(other), getCtx());
	}

	@Override
	public boolean isSubMap(IMap other) {
		return getMap().isSubMap(other);
	}

	@Override
	public Iterator<IValue> iterator() {
		return getMap().iterator();
	}

	@Override
	public Iterator<IValue> valueIterator() {
		return getMap().valueIterator();
	}

	@Override
	public Iterator<Entry<IValue, IValue>> entryIterator() {
		return getMap().entryIterator();
	}

    @Override
    public Stream<IValue> stream() {
        return StreamSupport.stream(spliterator(), false).map(e -> IRascalValueFactory.getInstance().tuple(e, get(e)));
    }

}
