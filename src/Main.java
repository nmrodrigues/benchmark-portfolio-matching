import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    /**
     * Iterate through each line of input.
     */
    public static void main(String[] args) throws IOException {
        InputStreamReader reader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(reader);
        String line;
        while ((line = in.readLine()) != null) {
            SortedMap<Asset, Asset> pfHoldings = new TreeMap<>(new AssetNameComp());
            SortedMap<Asset, Asset> bmHoldings = new TreeMap<>(new AssetNameComp());
            populateHoldings(line, pfHoldings, bmHoldings);
            matchBenchmark(pfHoldings, bmHoldings);
            printTransactions(pfHoldings);
            pfHoldings.clear();
            bmHoldings.clear();
        }
    }

    //    get data for portfolio and benchmark
    private static void populateHoldings(String line,
                                         Map<Asset, Asset> portfolioHoldings,
                                         Map<Asset, Asset> benchmarkHoldings) {
        String[] input = line.split(":");
        String[] portfolio = input[0].split("\\|");
        String[] benchmark = input[1].split("\\|");

        for (String s : portfolio) {
            String[] inputVals = s.split(",");
            Asset pfAsset = new Asset(inputVals[0], inputVals[1], Integer.parseInt(inputVals[2]));
            portfolioHoldings.put(pfAsset, pfAsset); // add asset to portfolio holdings
        }

        for (String s : benchmark) {
            String[] inputVals = s.split(",");
            Asset bmAsset = new Asset(inputVals[0], inputVals[1], Integer.parseInt(inputVals[2]));
            benchmarkHoldings.put(bmAsset, bmAsset); // add asset to benchmark holdings
        }
    }


    private static void matchBenchmark(Map<Asset, Asset> pfHoldings,
                                       Map<Asset, Asset> bmHoldings) {
        // Doing the following
        // 1. Selling assets in portfolio which are not in benchmark
        // 2. For matching assets - matching quantities
        for (Asset pfAsset : pfHoldings.keySet()) {
            Asset bmAsset = bmHoldings.get(pfAsset);
            if (bmAsset != null && bmAsset.getType().equals(pfAsset.getType())) { // found asset in benchmark
                if (bmAsset.getType().equals(pfAsset.getType())) { // if same type
                    if (bmAsset.getQty() > pfAsset.getQty()) {
                        pfAsset.setAction("BUY");
                        pfAsset.setAmtToBuy(bmAsset.getQty() - pfAsset.getQty());
                    } else if (bmAsset.getQty() < pfAsset.getQty()) {
                        pfAsset.setAction("SELL");
                        pfAsset.setAmtToSell(pfAsset.getQty() - bmAsset.getQty());
                    } else {
                        pfAsset.setAction("NONE");
                    }
                }
            } else if (bmAsset == null) {
                pfAsset.setAction("SELL");
                pfAsset.setAmtToSell(pfAsset.getQty());
            }
        }

        // Doing the following
        // 1. Buying assets in portfolio which are in the benchmark but not in portfolio
        for (Asset benchmarkAsset : bmHoldings.keySet()) {
            Asset portfolioAsset = pfHoldings.get(benchmarkAsset);
            if (portfolioAsset == null) {
                Asset pfAssetToBuy = new Asset(benchmarkAsset.getName(), benchmarkAsset.getType());
                pfAssetToBuy.setAction("BUY");
                pfAssetToBuy.setAmtToBuy(benchmarkAsset.getQty());
                pfHoldings.put(pfAssetToBuy, pfAssetToBuy);
            }
        }
    }

    // print transactions
    private static void printTransactions(Map<Asset, Asset> pfHoldings) {
        for (Asset pfAsset : pfHoldings.keySet()) {
            switch (pfAsset.getAction()) {
                case "BUY":
                    System.out.println(pfAsset.toString() + "," + pfAsset.getAmtToBuy());
                    break;
                case "SELL":
                    System.out.println(pfAsset.toString() + "," + pfAsset.getAmtToSell());
                    break;
                default: // no action required
            }
        }
    }
}

// Asset class
class Asset {
    private String name;
    private String type;
    private int qty;
    private String action;
    private int amtToBuy;
    private int amtToSell;

    Asset(String name, String type, int qty) {
        this.name = name;
        this.type = type;
        this.qty = qty;
    }

    Asset(String name, String type) {
        this.name = name;
        this.type = type;
    }

    // setters and getters
    int getAmtToBuy() {
        return amtToBuy;
    }

    void setAmtToBuy(int amtToBuy) {
        this.amtToBuy = amtToBuy;
    }

    int getAmtToSell() {
        return amtToSell;
    }

    void setAmtToSell(int amtToSell) {
        this.amtToSell = amtToSell;
    }

    String getName() {
        return name;
    }

    String getType() {
        return type;
    }

    int getQty() {
        return qty;
    }

    String getAction() {
        return action;
    }

    void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return action + "," + name + "," + type;
    }

    // equality based on asset name and type
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asset asset = (Asset) o;
        return name.equals(asset.name) &&
                type.equals(asset.type);
    }

    // hash code based on string variables
    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }
}

class AssetNameComp implements Comparator<Asset> {
    @Override
    public int compare(Asset o1, Asset o2) {
        String o1Val = o1.getName() + "-" + o1.getType();
        String o2Val = o2.getName() + "-" + o2.getType();
        return o1Val.compareTo(o2Val);
    }
}