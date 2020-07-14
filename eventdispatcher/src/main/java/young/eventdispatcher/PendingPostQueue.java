package young.eventdispatcher;

import java.util.Iterator;
import java.util.List;

final class PendingPostQueue {

    private PendingPost mHead = new PendingPost(-1, 0, null, null);
    private int mSize = 0;

    public void add(PendingPost post) {
        if (post == null || post.mPriority <= -1) {
            return;
        }
        PendingPost entry = mHead.mNext;
        for (int i = 0; i <= mSize; i++) {
            if (entry.mPriority <= post.mPriority) {
                mSize++;
                post.insert(entry.mPrev);
                return;
            }
            entry = entry.mNext;
        }
    }


    public Iterator<? extends PendingPost> iterator() {
        return new Iterator() {
            private PendingPost index = mHead;
            private PendingPost next = null;

            public boolean hasNext() {
                next = null;
                while (next == null) {
                    PendingPost nextIndex = index.mPrev;
                    if (nextIndex == mHead) {
                        break;
                    }
                    next = nextIndex;
                }
                return next != null;
            }

            public PendingPost next() {
                hasNext();
                index = index.mPrev;
                return next;
            }

            public void remove() {
                if (index != mHead) {
                    PendingPost nextIndex = index.mNext;
                    mSize--;
                    index.remove();
                    index = nextIndex;
                }
            }
        };
    }

    static class PendingPost {
        volatile PendingPost mPrev, mNext;
        int mPriority;
        int mMethodId;
        ThreadMode mMode;
        List<Object> mObjects;

        public PendingPost(int priority, int methodId, ThreadMode mode, List<Object> objects) {
            mPrev = this;
            mNext = this;
            mPriority = priority;
            mMethodId = methodId;
            mMode = mode;
            mObjects = objects;
        }

        public void insert(PendingPost where) {
            mPrev = where;
            mNext = where.mNext;
            where.mNext = this;
            mNext.mPrev = this;
        }

        public void remove() {
            mPrev.mNext = mNext;
            mNext.mPrev = mPrev;
            mNext = this;
            mPrev = this;
        }
    }
}
