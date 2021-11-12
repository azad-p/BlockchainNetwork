import pandas as pd

data = pd.read_csv("BitcoinHeistData.csv")

# sorting data frame by multiple columns
data.sort_values(["year", "day", "address"], axis=0,
                 ascending=True, inplace=True)

data.drop(["length","weight","count","looped","neighbors","income"],axis=1,inplace=True)


data.drop(data.index[(data["label"] == "white")],axis=0,inplace=True)
data.to_csv('SortedDataFinal.csv')




