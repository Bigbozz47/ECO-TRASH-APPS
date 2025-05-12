from ecotrash.firebase import db

def print_collections():
    print("📂 Koleksi Firestore:")
    for collection in db.collections():
        print("-", collection.id)

def print_users():
    print("\n📄 Daftar Users:")
    docs = db.collection('users').stream()
    for doc in docs:
        print(f"{doc.id} → {doc.to_dict()}")

def print_transactions():
    print("\n📄 Daftar Transaksi:")
    docs = db.collection('transactions').stream()
    for doc in docs:
        print(f"{doc.id} → {doc.to_dict()}")

def print_prices():
    print("\n♻️ Harga Sampah:")
    docs = db.collection('trash_prices').stream()
    for doc in docs:
        print(f"{doc.id} → {doc.to_dict()}")

def run():
    print_collections()
    print_users()
    print_transactions()
    print_prices()

if __name__ == "__main__":
    run()
