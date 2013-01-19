package clear.dep;

public class DepProj {

    public int count = 0;

    public void detectNonProjective(DepTree tree) {
        for (int currId = 1; currId < tree.size(); currId++) {
            DepNode curr = tree.get(currId);
            int sId, eId;

            if (curr.id < curr.headId) {
                sId = curr.id;
                eId = curr.headId;
            } else {
                sId = curr.headId;
                eId = curr.id;
            }

            for (int i = sId + 1; i < eId; i++) {
                DepNode node = tree.get(i);
                if (sId > node.headId || node.headId > eId) {
                    count++;
                    return;

                    //	curr.proj = 1;
                    //	node.proj = 1;
                }
            }
        }
    }
}