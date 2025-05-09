from rest_framework.permissions import BasePermission, SAFE_METHODS


class IsAdmin(BasePermission):
    """
    Hanya admin (Bank Sampah) yang boleh mengakses.
    """
    def has_permission(self, request, view):
        return bool(
            request.user and
            request.user.is_authenticated and
            request.user.role == 'admin'
        )


class IsNasabah(BasePermission):
    """
    Hanya nasabah yang boleh mengakses.
    """
    def has_permission(self, request, view):
        return bool(
            request.user and
            request.user.is_authenticated and
            request.user.role == 'nasabah'
        )


class IsAdminOrReadOnly(BasePermission):
    """
    Mengizinkan semua pengguna untuk metode baca (GET, HEAD, OPTIONS),
    hanya admin yang boleh mengubah (POST, PUT, PATCH, DELETE).
    """
    def has_permission(self, request, view):
        if request.method in SAFE_METHODS:
            return True
        return bool(
            request.user and
            request.user.is_authenticated and
            request.user.role == 'admin'
        )


class IsNasabahOrReadOnly(BasePermission):
    """
    Mengizinkan semua pengguna untuk metode baca (GET, HEAD, OPTIONS),
    hanya nasabah yang boleh mengubah (POST pada endpoint tertentu).
    Berguna misalnya untuk endpoint /api/setor/.
    """
    def has_permission(self, request, view):
        if request.method in SAFE_METHODS:
            return True
        return bool(
            request.user and
            request.user.is_authenticated and
            request.user.role == 'nasabah'
        )


class RoleBasedPermission(BasePermission):
    """
    Permission umum berdasarkan daftar role yang diizinkan.
    Atur attribute `allowed_roles = ['admin', 'nasabah']` di view.
    """
    def has_permission(self, request, view):
        allowed = getattr(view, 'allowed_roles', [])
        return bool(
            request.user and
            request.user.is_authenticated and
            request.user.role in allowed
        )
