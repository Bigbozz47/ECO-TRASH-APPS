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

def print_transactions(filter_email=None):
    print("\n📄 Daftar Transaksi:")
    query = db.collection('transactions')
    if filter_email:
        query = query.where('email', '==', filter_email)
    docs = query.stream()
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
    
    email = input("\n🔍 Masukkan email user untuk filter transaksi (atau tekan Enter untuk semua): ").strip()
    print_transactions(email if email else None)

    print_prices()

if __name__ == "__main__":
    run()
